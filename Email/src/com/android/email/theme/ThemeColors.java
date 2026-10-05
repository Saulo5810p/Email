package com.android.email.theme;

import android.content.Context;
import android.content.SharedPreferences;

/** Cores personalizadas: acento (laranja) e fundo (branco). Sem valor salvo = padrao do app. */
public final class ThemeColors {
    public static final int DEFAULT_ACCENT = 0xFFE7790D;
    public static final int DEFAULT_ACCENT_DARK = 0xFFD06D0C;
    public static final int DEFAULT_BACKGROUND = 0xFFFFFFFF;

    private static final String PREFS = "xaulinxs_theme_colors";
    private static final String K_ACCENT = "accent";
    private static final String K_BG = "background";

    private ThemeColors() {}

    private static SharedPreferences sp(Context c) {
        return c.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }

    public static int accent(Context c) { return sp(c).getInt(K_ACCENT, DEFAULT_ACCENT); }

    public static int background(Context c) { return sp(c).getInt(K_BG, DEFAULT_BACKGROUND); }

    public static int accentDark(Context c) {
        int a = accent(c);
        if (a == DEFAULT_ACCENT) return DEFAULT_ACCENT_DARK;
        return 0xFF000000
                | (((a >> 16 & 0xFF) * 85 / 100) << 16)
                | (((a >> 8 & 0xFF) * 85 / 100) << 8)
                | ((a & 0xFF) * 85 / 100);
    }

    public static boolean isCustomized(Context c) {
        return accent(c) != DEFAULT_ACCENT || background(c) != DEFAULT_BACKGROUND;
    }

    public static void save(Context c, int accent, int background) {
        sp(c).edit().putInt(K_ACCENT, accent).putInt(K_BG, background).apply();
    }

    public static void reset(Context c) { sp(c).edit().clear().apply(); }

    /** Aceita "#RGB", "#RRGGBB" (com ou sem '#'). Retorna null se invalido. */
    public static Integer parseHex(String text) {
        if (text == null) return null;
        String h = text.trim();
        if (h.startsWith("#")) h = h.substring(1);
        if (h.length() == 3) {
            h = "" + h.charAt(0) + h.charAt(0) + h.charAt(1) + h.charAt(1)
                    + h.charAt(2) + h.charAt(2);
        }
        if (h.length() != 6) return null;
        try {
            return 0xFF000000 | (int) Long.parseLong(h, 16);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String toHex(int color) {
        return String.format(java.util.Locale.US, "#%06X", color & 0xFFFFFF);
    }
}
