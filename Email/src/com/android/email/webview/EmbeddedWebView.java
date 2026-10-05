package com.android.email.webview;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

import com.android.email.R;
import com.norman.webviewup.lib.WebViewUpgrade;
import com.norman.webviewup.lib.source.UpgradeAssetSource;
import com.norman.webviewup.lib.source.UpgradeSource;
import com.norman.webviewup.lib.util.ProcessUtils;

import java.io.File;

/**
 * Troca o WebView do sistema pelo WebView AOSP embutido em assets/aosp_webview.apk
 * (biblioteca WebViewUpgrade, com.norman.webviewup.lib).
 *
 * Roda de forma SINCRONA em Application.onCreate(), no processo principal, antes de
 * qualquer WebView existir. Na primeira execucao (ou ao trocar o APK em assets) o
 * APK e copiado para filesDir, o que bloqueia a thread principal por alguns segundos.
 *
 * Fail-closed: se a troca falhar, {@link GatedWebView} se recusa a criar WebViews
 * (nunca cai silenciosamente no WebView do sistema), a nao ser que
 * {@link #ALLOW_SYSTEM_WEBVIEW_FALLBACK} seja alterado para true.
 */
public final class EmbeddedWebView {
    private static final String TAG = "EmbeddedWebView";

    public static final String ASSET_NAME = "aosp_webview.apk";

    /** false = nunca usar o WebView do sistema. Mude para true so para depurar. */
    public static final boolean ALLOW_SYSTEM_WEBVIEW_FALLBACK = false;

    private static final long COPY_TIMEOUT_MS = 90_000L;
    private static final String FP_PREFS = "xaulinxs_webview_asset_fingerprint";
    private static final String FP_KEY = "asset_length_bytes";

    private static volatile boolean sReady;
    private static volatile String sFailure;

    private EmbeddedWebView() {}

    /** true quando o WebView AOSP embutido foi carregado neste processo. */
    public static boolean isReady() { return sReady; }

    /** true quando e permitido criar WebViews (embutido pronto ou fallback liberado). */
    public static boolean isUsable() { return sReady || ALLOW_SYSTEM_WEBVIEW_FALLBACK; }

    public static String getFailure() { return sFailure; }

    public static void init(Application app) {
        // Application.onCreate roda de novo nos processos sandbox criados pela lib.
        if (!ProcessUtils.isMainProcess(app)) return;
        try {
            File apk = new File(app.getFilesDir(), ASSET_NAME);
            UpgradeAssetSource source = new UpgradeAssetSource(app, ASSET_NAME, apk);

            if (bundledApkChanged(app)) source.delete();

            if (!source.isSuccess()) {
                source.prepare(new UpgradeSource.OnPrepareCallback() {
                    @Override public void onPrepareSuccess(UpgradeSource s) {}
                    @Override public void onPrepareProcess(UpgradeSource s, float percent) {}
                    @Override public void onPrepareError(UpgradeSource s, Throwable t) {}
                });
                final long deadline = SystemClock.uptimeMillis() + COPY_TIMEOUT_MS;
                while (!source.isSuccess() && source.getError() == null
                        && SystemClock.uptimeMillis() < deadline) {
                    try {
                        Thread.sleep(25);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
            }
            if (!source.isSuccess()) {
                Throwable err = source.getError();
                sFailure = err != null ? String.valueOf(err) : "timeout copiando " + ASSET_NAME;
                Log.e(TAG, "WebView AOSP indisponivel: " + sFailure);
                return;
            }

            // Tudo sincrono: a copia ja terminou, entao a lib roda replace() na hora.
            WebViewUpgrade.upgrade(source);
            if (WebViewUpgrade.isCompleted()) {
                sReady = true;
            } else {
                Throwable err = WebViewUpgrade.getUpgradeError();
                sFailure = err != null ? String.valueOf(err) : "upgrade nao concluiu";
                Log.e(TAG, "Falha na troca do WebView: " + sFailure, err);
            }
        } catch (Throwable t) {
            sFailure = String.valueOf(t);
            Log.e(TAG, "Falha inesperada na troca do WebView", t);
        }
    }

    /** Compara o tamanho do APK em assets com o da ultima execucao. */
    private static boolean bundledApkChanged(Context c) {
        long len;
        try {
            len = c.getAssets().openFd(ASSET_NAME).getDeclaredLength();
        } catch (Exception e) {
            return false;
        }
        SharedPreferences sp = c.getSharedPreferences(FP_PREFS, Context.MODE_PRIVATE);
        long stored = sp.getLong(FP_KEY, -1L);
        sp.edit().putLong(FP_KEY, len).apply();
        return stored != len;
    }

    /** Texto do rodape das configuracoes: motor, versao e pacote. */
    public static String describe(Context c) {
        if (!sReady) return c.getString(R.string.xl_webview_footer_unavailable);
        String pkg = null, ver = null;
        try {
            File apk = new File(c.getFilesDir(), ASSET_NAME);
            PackageInfo pi = apk.exists()
                    ? c.getPackageManager().getPackageArchiveInfo(apk.getAbsolutePath(), 0) : null;
            if (pi != null) {
                pkg = pi.packageName;
                long code = Build.VERSION.SDK_INT >= 28 ? pi.getLongVersionCode() : pi.versionCode;
                ver = pi.versionName + " (" + code + ")";
            }
        } catch (Throwable ignored) {}
        if (pkg == null) pkg = WebViewUpgrade.getUpgradeWebViewPackageName();
        if (ver == null) ver = WebViewUpgrade.getUpgradeWebViewVersion();
        String label = "com.android.webview".equals(pkg) ? "AOSP WebView" : String.valueOf(pkg);
        return c.getString(R.string.xl_webview_footer_format, label,
                ver != null ? ver : "?", pkg != null ? pkg : "?");
    }
}
