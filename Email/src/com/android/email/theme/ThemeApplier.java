package com.android.email.theme;

import android.app.Activity;
import android.app.Application;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

import androidx.drawerlayout.widget.DrawerLayout;

import com.android.email.R;
import com.android.email.activity.setup.ThemeColorsActivity;

import java.util.HashSet;
import java.util.Set;

/**
 * Aplica as cores personalizadas em runtime. As cores laranja/branco do app vem de
 * @color/ em XML (nao mudam em runtime), entao a cada onResume percorremos a view
 * tree e trocamos os fundos que sao EXATAMENTE a cor original (ColorDrawable).
 * Sem personalizacao, nao faz nada. Cobertura: barra superior, barra de status,
 * fundos lisos, botao de escrever e cabecalho do widget. Itens de lista com
 * selector e texto nao sao alterados.
 */
public final class ThemeApplier implements Application.ActivityLifecycleCallbacks {
    private final Set<Integer> mAccents = new HashSet<>();
    private final Set<Integer> mDarks = new HashSet<>();
    private final Set<Integer> mBgs = new HashSet<>();
    private boolean mTouched;

    private ThemeApplier() {
        mAccents.add(ThemeColors.DEFAULT_ACCENT);
        mDarks.add(ThemeColors.DEFAULT_ACCENT_DARK);
        mBgs.add(ThemeColors.DEFAULT_BACKGROUND);
    }

    public static void install(Application app) {
        app.registerActivityLifecycleCallbacks(new ThemeApplier());
    }

    @Override public void onActivityResumed(final Activity a) {
        if (a instanceof ThemeColorsActivity) return;
        if (!mTouched && !ThemeColors.isCustomized(a)) return;
        final View decor = a.getWindow().getDecorView();
        final Runnable r = new Runnable() { @Override public void run() { apply(a, decor); } };
        apply(a, decor);
        // fragments/listas carregam depois do primeiro frame
        decor.postDelayed(r, 400);
        decor.postDelayed(r, 1500);
    }

    private void apply(Activity a, View decor) {
        if (a.isFinishing()) return;
        final int accent = ThemeColors.accent(a);
        final int dark = ThemeColors.accentDark(a);
        final int bg = ThemeColors.background(a);
        mTouched = true;
        walk(decor, accent, dark, bg);
        mAccents.add(accent);
        mDarks.add(dark);
        mBgs.add(bg);
        final Window w = a.getWindow();
        if (Build.VERSION.SDK_INT >= 21 && w != null && mDarks.contains(w.getStatusBarColor())) {
            w.setStatusBarColor(dark);
        }
        tintComposeButton(decor, accent, dark);
    }

    private void walk(View v, int accent, int dark, int bg) {
        final Drawable d = v.getBackground();
        if (d instanceof ColorDrawable) {
            final int c = ((ColorDrawable) d).getColor();
            if (mAccents.contains(c)) {
                if (c != accent) v.setBackgroundColor(accent);
            } else if (mDarks.contains(c)) {
                if (c != dark) v.setBackgroundColor(dark);
            } else if (mBgs.contains(c)) {
                if (c != bg) v.setBackgroundColor(bg);
            }
        }
        if (v instanceof DrawerLayout) {
            ((DrawerLayout) v).setStatusBarBackgroundColor(dark);
        }
        if (v instanceof ViewGroup) {
            final ViewGroup g = (ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) walk(g.getChildAt(i), accent, dark, bg);
        }
    }

    private void tintComposeButton(View decor, int accent, int dark) {
        if (Build.VERSION.SDK_INT < 23) return;
        final View fab = decor.findViewById(R.id.compose_button);
        if (fab == null || !(fab.getBackground() instanceof LayerDrawable)) return;
        final LayerDrawable ld = (LayerDrawable) fab.getBackground();
        if (ld.getNumberOfLayers() < 2) return;
        final int size = fab.getResources().getDimensionPixelSize(R.dimen.compose_button_width);
        final StateListDrawable sl = new StateListDrawable();
        sl.addState(new int[] {android.R.attr.state_pressed}, oval(dark, size));
        sl.addState(new int[] {android.R.attr.state_focused}, oval(dark, size));
        sl.addState(new int[] {}, oval(accent, size));
        ld.setDrawable(0, sl);
    }

    private static GradientDrawable oval(int color, int size) {
        final GradientDrawable g = new GradientDrawable();
        g.setShape(GradientDrawable.OVAL);
        g.setColor(color);
        g.setSize(size, size);
        return g;
    }

    @Override public void onActivityCreated(Activity a, Bundle b) {}
    @Override public void onActivityStarted(Activity a) {}
    @Override public void onActivityPaused(Activity a) {}
    @Override public void onActivityStopped(Activity a) {}
    @Override public void onActivitySaveInstanceState(Activity a, Bundle b) {}
    @Override public void onActivityDestroyed(Activity a) {}
}
