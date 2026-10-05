package com.android.mail.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

/**
 * Permissao de notificacao do Android 13+ (POST_NOTIFICATIONS, obrigatoria com targetSdk 33+).
 *
 * Pede UMA vez (ao abrir a tela principal). Se o usuario negar, nao insistimos: ele pode ligar
 * depois em Configuracoes do sistema > Apps > Email > Notificacoes. Abaixo do Android 13 nao
 * existe permissao em tempo de execucao e nada acontece.
 */
public final class NotificationPermission {
    private static final String PREFS = "xl_notification_permission";
    private static final String KEY_ASKED = "asked_once";
    public static final int REQUEST_CODE = 0x4E54; // "NT"

    private NotificationPermission() {}

    public static boolean isGranted(Context context) {
        if (Build.VERSION.SDK_INT < 33) return true;
        return context.checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestOnce(Activity activity) {
        if (Build.VERSION.SDK_INT < 33) return;
        if (isGranted(activity)) return;
        final SharedPreferences sp = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (sp.getBoolean(KEY_ASKED, false)) return;
        sp.edit().putBoolean(KEY_ASKED, true).apply();
        activity.requestPermissions(new String[] {Manifest.permission.POST_NOTIFICATIONS},
                REQUEST_CODE);
    }
}
