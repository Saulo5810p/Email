/*
 * Cifra/decifra segredos (senha de conta, tokens OAuth) antes de irem para o disco.
 *
 * - Algoritmo: AES-256-GCM, chave gerada e guardada no Android Keystore (a chave nunca
 *   sai do Keystore; o app so pede "cifre/decifre isto").
 * - Formato gravado: "enc1:" + Base64(IV[12] + texto cifrado + tag).
 * - Valor sem o prefixo "enc1:" e tratado como legado em texto puro (migracao suave):
 *   continua funcionando e passa a ser gravado cifrado na proxima vez que for salvo.
 * - API < 23 (Android 5.x): o Keystore nao suporta AES; la o valor segue em texto puro.
 */
package com.android.emailcommon.utility;

import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;

import com.android.mail.utils.LogUtils;

import java.nio.charset.StandardCharsets;
import java.security.KeyStore;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

public final class SecretStore {
    public static final String PREFIX = "enc1:";

    private SecretStore() {}

    /** true quando este aparelho consegue cifrar (API 23+). */
    public static boolean isSupported() {
        return Build.VERSION.SDK_INT >= 23;
    }

    /** true quando o valor ja esta no formato cifrado. */
    public static boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    /**
     * Cifra um segredo em texto puro. null/vazio voltam como estao.
     * Falha fechada: se o Keystore nao funcionar, lanca excecao em vez de gravar em texto puro.
     */
    public static String encrypt(String plain) {
        if (plain == null || plain.isEmpty()) return plain;
        if (!isSupported()) return plain;
        try {
            return Aes.encrypt(plain);
        } catch (Exception e) {
            LogUtils.e(LogUtils.TAG, e, "SecretStore: falha ao cifrar (segredo NAO gravado)");
            throw new IllegalStateException("Keystore indisponivel: segredo nao foi gravado", e);
        }
    }

    /**
     * Decifra um valor lido do disco. Sem prefixo = legado em texto puro, devolvido como esta.
     * Se nao for possivel decifrar (chave perdida/invalidada), devolve null e o app
     * pede a senha de novo, em vez de travar.
     */
    public static String decrypt(String stored) {
        if (stored == null || !stored.startsWith(PREFIX)) return stored;
        if (!isSupported()) return null;
        try {
            return Aes.decrypt(stored.substring(PREFIX.length()));
        } catch (Exception e) {
            LogUtils.e(LogUtils.TAG, e, "SecretStore: falha ao decifrar (chave ausente/invalidada?)");
            return null;
        }
    }

    /** Isolado numa classe interna: so e carregada em API 23+. */
    private static final class Aes {
        private static final String PROVIDER = "AndroidKeyStore";
        private static final String ALIAS = "xaulinxs_email_secrets_v1";
        private static final String TRANSFORMATION = "AES/GCM/NoPadding";
        private static final int IV_BYTES = 12;
        private static final int TAG_BITS = 128;

        private static synchronized SecretKey key(boolean createIfMissing) throws Exception {
            final KeyStore ks = KeyStore.getInstance(PROVIDER);
            ks.load(null);
            final KeyStore.Entry entry = ks.getEntry(ALIAS, null);
            if (entry instanceof KeyStore.SecretKeyEntry) {
                return ((KeyStore.SecretKeyEntry) entry).getSecretKey();
            }
            if (!createIfMissing) {
                throw new IllegalStateException("chave do Keystore ausente");
            }
            final KeyGenerator gen =
                    KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVIDER);
            gen.init(new KeyGenParameterSpec.Builder(ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build());
            return gen.generateKey();
        }

        static String encrypt(String plain) throws Exception {
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, key(true));
            final byte[] iv = cipher.getIV();
            final byte[] ct = cipher.doFinal(plain.getBytes(StandardCharsets.UTF_8));
            final byte[] out = new byte[iv.length + ct.length];
            System.arraycopy(iv, 0, out, 0, iv.length);
            System.arraycopy(ct, 0, out, iv.length, ct.length);
            return PREFIX + Base64.encodeToString(out, Base64.NO_WRAP);
        }

        static String decrypt(String base64) throws Exception {
            final byte[] all = Base64.decode(base64, Base64.NO_WRAP);
            if (all.length <= IV_BYTES) {
                throw new IllegalArgumentException("payload cifrado curto demais");
            }
            final Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, key(false),
                    new GCMParameterSpec(TAG_BITS, all, 0, IV_BYTES));
            final byte[] pt = cipher.doFinal(all, IV_BYTES, all.length - IV_BYTES);
            return new String(pt, StandardCharsets.UTF_8);
        }
    }
}
