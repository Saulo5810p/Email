package com.android.mail.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import com.android.email.R;

/**
 * Canais de notificacao (obrigatorios no Android 8+; sem canal a notificacao NAO aparece).
 * Criados em Application.onCreate. Criar um canal que ja existe e inofensivo.
 *
 * Som/vibracao por conta (ringtone/vibrar das configuracoes antigas) nao se aplicam no
 * Android 8+: ali o usuario ajusta isso por canal, em Configuracoes do sistema > Apps >
 * Email > Notificacoes.
 */
public final class NotificationChannels {
    /** Novos e-mails. */
    public static final String MAIL = "xl_mail_new";
    /** Avisos da conta: falha de login, erro de sincronizacao, politica de seguranca. */
    public static final String ALERTS = "xl_mail_alerts";
    /** Barra de "desfazer" (arquivar/excluir pela notificacao): silenciosa. */
    public static final String ACTIONS = "xl_mail_actions";

    private NotificationChannels() {}

    public static void ensureCreated(Context context) {
        if (Build.VERSION.SDK_INT < 26) return;
        final NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) return;

        final NotificationChannel mail = new NotificationChannel(MAIL,
                context.getString(R.string.xl_channel_mail_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        mail.setDescription(context.getString(R.string.xl_channel_mail_desc));
        mail.setShowBadge(true);

        final NotificationChannel alerts = new NotificationChannel(ALERTS,
                context.getString(R.string.xl_channel_alerts_name),
                NotificationManager.IMPORTANCE_DEFAULT);
        alerts.setDescription(context.getString(R.string.xl_channel_alerts_desc));

        final NotificationChannel actions = new NotificationChannel(ACTIONS,
                context.getString(R.string.xl_channel_actions_name),
                NotificationManager.IMPORTANCE_LOW);
        actions.setDescription(context.getString(R.string.xl_channel_actions_desc));
        actions.setShowBadge(false);

        nm.createNotificationChannel(mail);
        nm.createNotificationChannel(alerts);
        nm.createNotificationChannel(actions);
    }
}
