#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
aplicar_webview_email.py  —  Email AOSP: WebView AOSP embutido + rodape de versao
                              + tela de cores + .gitignore + README

Uso (na raiz do repo Email, onde ficam as pastas Email/ e UnifiedEmail/):

    python aplicar_webview_email.py                       # acha o ~/Navegador sozinho
    python aplicar_webview_email.py --navegador ~/Navegador
    python aplicar_webview_email.py --untrack             # tambem tira do indice do git
                                                          # os arquivos que o .gitignore cobre

E idempotente: pode rodar quantas vezes quiser; so altera o que ainda falta.
"""
import os, sys, re, shutil, subprocess, glob

ROOT = os.getcwd()
args = sys.argv[1:]
NAV = None
if '--navegador' in args:
    NAV = os.path.expanduser(args[args.index('--navegador') + 1])
UNTRACK = '--untrack' in args

changed = []
def log(msg): print(msg)

def p(*parts): return os.path.join(ROOT, *parts)

def read(path):
    with open(path, 'r', encoding='utf-8') as f: return f.read()

def write(path, content, only_if_missing=False):
    if os.path.exists(path):
        if only_if_missing: return False
        if read(path) == content: return False
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, 'w', encoding='utf-8', newline='\n') as f: f.write(content)
    changed.append(os.path.relpath(path, ROOT)); return True

def edit(path, old, new, marker=None, count=1):
    """Troca `old` por `new` uma vez. Idempotente via `marker` (ou `new`)."""
    if not os.path.exists(path):
        log('  !! arquivo nao existe: ' + path); return False
    s = read(path)
    if (marker or new) in s: return False
    if s.count(old) < 1:
        log('  !! ancora nao encontrada em %s: %r' % (os.path.relpath(path, ROOT), old[:60])); return False
    s = s.replace(old, new, count)
    with open(path, 'w', encoding='utf-8', newline='\n') as f: f.write(s)
    changed.append(os.path.relpath(path, ROOT)); return True

if not (os.path.isfile(p('Email', 'app', 'build.gradle')) and os.path.isdir(p('UnifiedEmail', 'src'))):
    sys.exit('ERRO: rode na raiz do repo Email (onde ficam Email/ e UnifiedEmail/).')

# ------------------------------------------------------------------ 1. lib + assets do Navegador
def find_nav():
    cands = []
    if NAV: cands.append(NAV)
    if os.environ.get('NAVEGADOR_DIR'): cands.append(os.environ['NAVEGADOR_DIR'])
    home = os.path.expanduser('~')
    cands += [os.path.join(ROOT, '..', 'Navegador'), os.path.join(home, 'Navegador'),
              os.path.join(home, 'storage', 'shared', 'Navegador')]
    cands += glob.glob(os.path.join(home, '*', 'Navegador')) + glob.glob(os.path.join(home, '*', '*', 'Navegador'))
    for c in cands:
        if os.path.isfile(os.path.join(c, 'app', 'src', 'main', 'java', 'com', 'norman', 'webviewup', 'lib', 'WebViewUpgrade.java')):
            return os.path.abspath(c)
    return None

nav = find_nav()
LIB_DST = p('Email', 'src', 'com', 'norman', 'webviewup')
ASSETS_DST = p('Email', 'app', 'src', 'main', 'assets')
if nav is None and not os.path.isfile(os.path.join(LIB_DST, 'lib', 'WebViewUpgrade.java')):
    sys.exit('ERRO: nao achei o repo Navegador. Rode com: --navegador /caminho/do/Navegador')
if nav:
    log('Navegador: ' + nav)
    lib_src = os.path.join(nav, 'app', 'src', 'main', 'java', 'com', 'norman', 'webviewup')
    n = 0
    for dp, dn, fn in os.walk(lib_src):
        for f in fn:
            s = os.path.join(dp, f); d = os.path.join(LIB_DST, os.path.relpath(s, lib_src))
            if not os.path.exists(d) or read(s) != read(d):
                os.makedirs(os.path.dirname(d), exist_ok=True); shutil.copyfile(s, d); n += 1
    if n: changed.append('Email/src/com/norman/webviewup (%d arquivos)' % n)
    as_src = os.path.join(nav, 'app', 'src', 'main', 'assets')
    n = 0
    for dp, dn, fn in os.walk(as_src):
        dn[:] = [x for x in dn if x != 'adblock']
        for f in fn:
            if f == 'LEIA-ME.txt' or f.lower().endswith('.apk'): continue
            s = os.path.join(dp, f); d = os.path.join(ASSETS_DST, os.path.relpath(s, as_src))
            if not os.path.exists(d) or os.path.getsize(d) != os.path.getsize(s):
                os.makedirs(os.path.dirname(d), exist_ok=True); shutil.copyfile(s, d); n += 1
    if n: changed.append('Email/app/src/main/assets (%d arquivos .pak/.dat/.bin)' % n)

write(os.path.join(ASSETS_DST, 'LEIA-ME.txt'),
"""Copie aqui o APK do WebView AOSP (qualquer versao, nova ou antiga)
renomeado exatamente para:

    aosp_webview.apk

Caminho completo esperado:
    Email/app/src/main/assets/aosp_webview.apk

O .apk NAO vai para o GitHub (esta no .gitignore - arquivo grande demais).
Sem ele o app NAO usa o WebView do sistema: abrir um e-mail falha de
proposito (modo fail-closed). Veja EmbeddedWebView.java.
""", only_if_missing=True)

# ------------------------------------------------------------------ 2. classes novas
write(p('Email', 'src', 'com', 'android', 'email', 'webview', 'EmbeddedWebView.java'), r'''package com.android.email.webview;

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
''')

write(p('Email', 'src', 'com', 'android', 'email', 'webview', 'GatedWebView.java'), r'''package com.android.email.webview;

import android.content.Context;
import android.util.AttributeSet;
import android.webkit.WebSettings;
import android.webkit.WebView;

/**
 * Ponto unico de criacao de WebView do app. Recusa criar a view se o WebView AOSP
 * embutido nao foi carregado (fail-closed) e aplica padroes de seguranca que nenhum
 * conteudo de e-mail precisa desligar.
 */
public class GatedWebView extends WebView {

    public GatedWebView(Context context) {
        this(context, null);
    }

    public GatedWebView(Context context, AttributeSet attrs) {
        super(requireUsable(context), attrs);
        hardenDefaults();
    }

    public GatedWebView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(requireUsable(context), attrs, defStyleAttr);
        hardenDefaults();
    }

    private static Context requireUsable(Context context) {
        if (!EmbeddedWebView.isUsable()) {
            throw new IllegalStateException("WebView AOSP embutido indisponivel ("
                    + EmbeddedWebView.getFailure() + "). Coloque aosp_webview.apk em "
                    + "Email/app/src/main/assets e recompile. Uso do WebView do sistema "
                    + "bloqueado (EmbeddedWebView.ALLOW_SYSTEM_WEBVIEW_FALLBACK).");
        }
        return context;
    }

    private void hardenDefaults() {
        final WebSettings s = getSettings();
        s.setGeolocationEnabled(false);
        s.setAllowFileAccessFromFileURLs(false);
        s.setAllowUniversalAccessFromFileURLs(false);
        s.setJavaScriptCanOpenWindowsAutomatically(false);
        s.setSupportMultipleWindows(false);
        s.setSaveFormData(false);
    }
}
''')

write(p('Email', 'src', 'com', 'android', 'email', 'theme', 'ThemeColors.java'), r'''package com.android.email.theme;

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
''')

write(p('Email', 'src', 'com', 'android', 'email', 'theme', 'ThemeApplier.java'), r'''package com.android.email.theme;

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
''')

write(p('Email', 'src', 'com', 'android', 'email', 'activity', 'setup', 'ThemeColorsActivity.java'), r'''package com.android.email.activity.setup;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputFilter;
import android.text.TextWatcher;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.email.R;
import com.android.email.theme.ThemeColors;

/** Tela simples: editor hex para a cor de acento (laranja) e para o fundo (branco). */
public class ThemeColorsActivity extends Activity {
    private EditText mAccent;
    private EditText mBg;
    private View mAccentSwatch;
    private View mBgSwatch;

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v,
                getResources().getDisplayMetrics());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.xl_theme_colors_title);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

        final LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(dp(20), dp(16), dp(20), dp(16));

        final TextView note = new TextView(this);
        note.setText(R.string.xl_theme_note);
        note.setPadding(0, 0, 0, dp(16));
        root.addView(note);

        mAccent = addEditor(root, R.string.xl_theme_accent_label, ThemeColors.accent(this), true);
        mBg = addEditor(root, R.string.xl_theme_bg_label, ThemeColors.background(this), false);

        final LinearLayout buttons = new LinearLayout(this);
        buttons.setOrientation(LinearLayout.HORIZONTAL);
        buttons.setPadding(0, dp(20), 0, 0);
        final Button save = new Button(this);
        save.setText(R.string.xl_theme_save);
        save.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) { onSave(); }
        });
        final Button reset = new Button(this);
        reset.setText(R.string.xl_theme_reset);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                ThemeColors.reset(ThemeColorsActivity.this);
                Toast.makeText(ThemeColorsActivity.this, R.string.xl_theme_saved,
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        });
        buttons.addView(save);
        buttons.addView(reset);
        root.addView(buttons);

        final ScrollView sv = new ScrollView(this);
        sv.addView(root, new ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setContentView(sv);
    }

    private EditText addEditor(LinearLayout root, int labelRes, int color, final boolean accent) {
        final TextView label = new TextView(this);
        label.setText(labelRes);
        root.addView(label);

        final LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(0, dp(4), 0, dp(14));

        final EditText et = new EditText(this);
        et.setSingleLine(true);
        et.setHint(R.string.xl_theme_hex_hint);
        et.setFilters(new InputFilter[] {new InputFilter.LengthFilter(7)});
        et.setText(ThemeColors.toHex(color));
        row.addView(et, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        final View swatch = new View(this);
        swatch.setBackgroundColor(color);
        final LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(dp(48), dp(48));
        lp.leftMargin = dp(12);
        row.addView(swatch, lp);
        root.addView(row);

        if (accent) mAccentSwatch = swatch; else mBgSwatch = swatch;
        et.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void onTextChanged(CharSequence s, int a, int b, int c) {}
            @Override public void afterTextChanged(Editable e) {
                final Integer c = ThemeColors.parseHex(e.toString());
                if (c != null) {
                    (accent ? mAccentSwatch : mBgSwatch).setBackgroundColor(c);
                }
            }
        });
        return et;
    }

    private void onSave() {
        final Integer a = ThemeColors.parseHex(mAccent.getText().toString());
        final Integer b = ThemeColors.parseHex(mBg.getText().toString());
        boolean ok = true;
        if (a == null) { mAccent.setError(getString(R.string.xl_theme_invalid)); ok = false; }
        if (b == null) { mBg.setError(getString(R.string.xl_theme_invalid)); ok = false; }
        if (!ok) return;
        ThemeColors.save(this, a, b);
        Toast.makeText(this, R.string.xl_theme_saved, Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
''')

# ------------------------------------------------------------------ 3. strings
write(p('Email', 'res', 'values', 'xaulinxs_strings.xml'), '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="xl_webview_footer_format">WebView engine: %1$s %2$s  •  %3$s</string>
    <string name="xl_webview_footer_unavailable">WebView engine: AOSP WebView not loaded (aosp_webview.apk missing or failed)</string>
    <string name="xl_theme_colors_title">Colors</string>
    <string name="xl_theme_colors_summary">Accent color and background (hex)</string>
    <string name="xl_theme_accent_label">Accent color (orange)</string>
    <string name="xl_theme_bg_label">Background color (white)</string>
    <string name="xl_theme_hex_hint">#RRGGBB</string>
    <string name="xl_theme_save">Save</string>
    <string name="xl_theme_reset">Restore defaults</string>
    <string name="xl_theme_invalid">Invalid color. Use #RRGGBB</string>
    <string name="xl_theme_saved">Saved. Colors apply as screens are reopened.</string>
    <string name="xl_theme_note">Only the accent and plain backgrounds are changed. Text colors are not adjusted automatically, so avoid very dark backgrounds.</string>
</resources>
''')
write(p('Email', 'res', 'values-pt', 'xaulinxs_strings.xml'), '''<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="xl_webview_footer_format">Motor WebView: %1$s %2$s  •  %3$s</string>
    <string name="xl_webview_footer_unavailable">Motor WebView: WebView AOSP não carregado (aosp_webview.apk ausente ou com falha)</string>
    <string name="xl_theme_colors_title">Cores</string>
    <string name="xl_theme_colors_summary">Cor de destaque e fundo (hex)</string>
    <string name="xl_theme_accent_label">Cor de destaque (laranja)</string>
    <string name="xl_theme_bg_label">Cor de fundo (branco)</string>
    <string name="xl_theme_hex_hint">#RRGGBB</string>
    <string name="xl_theme_save">Salvar</string>
    <string name="xl_theme_reset">Restaurar padrão</string>
    <string name="xl_theme_invalid">Cor inválida. Use #RRGGBB</string>
    <string name="xl_theme_saved">Salvo. As cores valem ao reabrir as telas.</string>
    <string name="xl_theme_note">Só o destaque e os fundos lisos são alterados. A cor do texto não muda sozinha; evite fundos muito escuros.</string>
</resources>
''')

# ------------------------------------------------------------------ 4. cabecalho de configuracoes (+ rodape)
edit(p('Email', 'res', 'xml', 'email_extra_preference_headers.xml'),
     '''            android:title="@string/add_account" />
''',
     '''            android:title="@string/add_account" />

    <header android:id="@+id/theme_colors_header"
            android:title="@string/xl_theme_colors_title"
            android:summary="@string/xl_theme_colors_summary">
        <intent android:action="android.intent.action.MAIN"
                android:targetPackage="com.android.email"
                android:targetClass="com.android.email.activity.setup.ThemeColorsActivity" />
    </header>
''', marker='theme_colors_header')

EPA = p('Email', 'src', 'com', 'android', 'email', 'activity', 'setup', 'EmailPreferenceActivity.java')
edit(EPA, 'import android.view.MenuItem;\n',
     'import android.view.MenuItem;\nimport android.widget.TextView;\n', marker='import android.widget.TextView;')
edit(EPA, 'import com.android.email.setup.AuthenticatorSetupIntentHelper;\n',
     'import com.android.email.setup.AuthenticatorSetupIntentHelper;\nimport com.android.email.webview.EmbeddedWebView;\n',
     marker='import com.android.email.webview.EmbeddedWebView;')
edit(EPA, '''        mFeedbackUri = Utils.getValidUri(getString(R.string.email_feedback_uri));
    }
''', '''        mFeedbackUri = Utils.getValidUri(getString(R.string.email_feedback_uri));

        // Rodape: versao do WebView AOSP embutido.
        try {
            final TextView footer = new TextView(this);
            footer.setText(EmbeddedWebView.describe(this));
            footer.setTextSize(12);
            footer.setAlpha(0.7f);
            final int pad = (int) (16 * getResources().getDisplayMetrics().density);
            footer.setPadding(pad, pad, pad, pad);
            setListFooter(footer);
        } catch (RuntimeException ignored) {
            // layout sem list_footer: sem rodape
        }
    }
''', marker='Rodape: versao do WebView AOSP')

# ------------------------------------------------------------------ 5. EmailApplication
EA = p('Email', 'src', 'com', 'android', 'email', 'EmailApplication.java')
edit(EA, 'import android.content.Intent;\n',
     'import android.content.Intent;\n', marker='import android.content.Intent;')
edit(EA, 'import com.android.email.preferences.EmailPreferenceMigrator;\n',
     'import com.android.email.preferences.EmailPreferenceMigrator;\nimport com.android.email.theme.ThemeApplier;\nimport com.android.email.webview.EmbeddedWebView;\n',
     marker='import com.android.email.webview.EmbeddedWebView;')
if os.path.exists(EA) and 'EmbeddedWebView.init(this)' not in read(EA):
    s = read(EA); i = s.rstrip().rfind('}')
    s = s[:i] + '''
    @Override
    public void onCreate() {
        super.onCreate();
        // Tem que rodar antes de qualquer WebView existir (processo principal).
        EmbeddedWebView.init(this);
        ThemeApplier.install(this);
    }
}
'''
    with open(EA, 'w', encoding='utf-8', newline='\n') as f: f.write(s)
    changed.append(os.path.relpath(EA, ROOT))

# ------------------------------------------------------------------ 6. pontos de criacao de WebView
GW = 'com.android.email.webview.GatedWebView'
BR = p('UnifiedEmail', 'src', 'com', 'android', 'mail', 'browse')
edit(os.path.join(BR, 'MailWebView.java'), 'public class MailWebView extends WebView {',
     'public class MailWebView extends GatedWebView {', marker='extends GatedWebView')
edit(os.path.join(BR, 'MailWebView.java'), 'import android.webkit.WebView;\n',
     'import android.webkit.WebView;\n\nimport ' + GW + ';\n', marker='import ' + GW)
edit(os.path.join(BR, 'MessageWebView.java'), 'public class MessageWebView extends WebView implements',
     'public class MessageWebView extends GatedWebView implements', marker='extends GatedWebView')
edit(os.path.join(BR, 'MessageWebView.java'), 'import android.webkit.WebView;\n',
     'import android.webkit.WebView;\nimport ' + GW + ';\n', marker='import ' + GW)

for rel in ('UnifiedEmail/res/layout/quoted_text.xml', 'UnifiedEmail/res/layout/licenses_activity.xml',
            'UnifiedEmail/res/layout/help_fragment.xml'):
    f = p(*rel.split('/'))
    if os.path.exists(f):
        s = read(f)
        if '<WebView' in s:
            with open(f, 'w', encoding='utf-8', newline='\n') as fh: fh.write(s.replace('<WebView', '<' + GW))
            changed.append(rel)

PU = p('UnifiedEmail', 'src', 'com', 'android', 'mail', 'print', 'PrintUtils.java')
edit(PU, 'new WebView(context)', 'new ' + GW + '(context)', marker='new ' + GW)
edit(PU, 'import android.webkit.WebView;\n', 'import android.webkit.WebView;\nimport ' + GW + ';\n', marker='import ' + GW)
OA = p('Email', 'src', 'com', 'android', 'email', 'activity', 'setup', 'OAuthAuthenticationActivity.java')
edit(OA, 'new WebView(this)', 'new ' + GW + '(this)', marker='new ' + GW)
edit(OA, 'import android.webkit.WebView;\n', 'import android.webkit.WebView;\nimport ' + GW + ';\n', marker='import ' + GW)
DF = p('Email', 'src', 'com', 'android', 'email', 'activity', 'setup', 'DebugFragment.java')
edit(DF, 'new WebView(getActivity())', 'new ' + GW + '(getActivity())', marker='new ' + GW)
edit(DF, 'import android.webkit.WebView;\n', 'import android.webkit.WebView;\nimport ' + GW + ';\n', marker='import ' + GW)

# renderer morto nao derruba o app
ACW = p('UnifiedEmail', 'src', 'com', 'android', 'mail', 'ui', 'AbstractConversationWebViewClient.java')
edit(ACW, '''    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {''',
'''    @Override
    @android.annotation.TargetApi(26)
    public boolean onRenderProcessGone(WebView view, android.webkit.RenderProcessGoneDetail detail) {
        // Processo de renderizacao morreu (ou foi morto): evita derrubar o app inteiro.
        return true;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {''', marker='onRenderProcessGone')

# varre qualquer outro WebView criado direto
for base in ('UnifiedEmail/src', 'UnifiedEmail/res', 'Email/src', 'Email/res'):
    for dp, dn, fn in os.walk(p(*base.split('/'))):
        for f in fn:
            if f.endswith(('.java', '.xml', '.kt')) and 'GatedWebView' not in f and 'webviewup' not in dp:
                t = read(os.path.join(dp, f))
                if re.search(r'new\s+WebView\s*\(|<WebView[\s>]', t):
                    log('  !! WebView direto ainda em: ' + os.path.relpath(os.path.join(dp, f), ROOT))

# ------------------------------------------------------------------ 7. cores: status bar + widget
edit(p('UnifiedEmail', 'src', 'com', 'android', 'mail', 'utils', 'ViewUtils.java'),
     '                window.setStatusBarColor(activity.getResources().getColor(colorId));',
     '''                int color = activity.getResources().getColor(colorId);
                if (color == com.android.email.theme.ThemeColors.DEFAULT_ACCENT_DARK
                        && com.android.email.theme.ThemeColors.isCustomized(activity)) {
                    color = com.android.email.theme.ThemeColors.accentDark(activity);
                }
                window.setStatusBarColor(color);''', marker='ThemeColors.DEFAULT_ACCENT_DARK')
edit(p('UnifiedEmail', 'src', 'com', 'android', 'mail', 'widget', 'WidgetService.java'),
     '        remoteViews.setOnClickPendingIntent(R.id.widget_header, clickIntent);\n',
     '''        remoteViews.setOnClickPendingIntent(R.id.widget_header, clickIntent);
        if (com.android.email.theme.ThemeColors.isCustomized(context)) {
            remoteViews.setInt(R.id.widget_header, "setBackgroundColor",
                    com.android.email.theme.ThemeColors.accent(context));
        }
''', marker='ThemeColors.isCustomized(context)')

# ------------------------------------------------------------------ 8. manifest
MF = p('Email', 'app', 'src', 'main', 'AndroidManifest.xml')
svc = ''.join('''        <service
            android:name="com.norman.webviewup.lib.sandbox.stub.StubSandboxedProcessService%d"
            android:exported="false"
            android:process=":sandboxed_process%d" />
''' % (i, i) for i in range(5))
edit(MF, '    </application>', '''
        <!-- Processos sandbox usados pela lib WebViewUpgrade (WebView AOSP embutido) -->
''' + svc + '''
        <activity
            android:name="com.android.email.activity.setup.ThemeColorsActivity"
            android:label="@string/xl_theme_colors_title"
            android:exported="false"
            android:theme="@android:style/Theme.DeviceDefault.Light.DarkActionBar" />
    </application>''', marker='StubSandboxedProcessService0')

# ------------------------------------------------------------------ 9. gradle + proguard
BG = p('Email', 'app', 'build.gradle')
edit(BG, 'assets.srcDirs("../../UnifiedEmail/assets")',
     'assets.srcDirs("../../UnifiedEmail/assets", "src/main/assets")', marker='"src/main/assets"')
edit(BG, '    lint {', '''    // O WebViewUpgrade precisa ler assets/aosp_webview.apk como bloco contiguo.
    androidResources {
        noCompress 'apk'
    }

    lint {''', marker="noCompress 'apk'")
PG = p('Email', 'app', 'proguard.flags')
keep = '''
# WebView AOSP embutido (WebViewUpgrade usa reflexao e Proxy)
-keep class com.norman.webviewup.** { *; }
-dontwarn com.norman.webviewup.**
'''
if not os.path.exists(PG):
    write(PG, keep.lstrip())
elif 'com.norman.webviewup' not in read(PG):
    with open(PG, 'a', encoding='utf-8', newline='\n') as f: f.write(keep)
    changed.append(os.path.relpath(PG, ROOT))

# ------------------------------------------------------------------ 10. .gitignore
GI_MARK = '# --- xaulinxs-gitignore ---'
GI = GI_MARK + r'''
# Build e cache de build
build/
**/build/
.gradle/
**/.gradle/
.cxx/
.externalNativeBuild/
captures/
*.class
*.dex
*.ap_
*.aab
*.apk
*.hprof
# (o APK do WebView AOSP fica so local: assets/aosp_webview.apk, grande demais pro GitHub)

# Informacoes sensiveis / chaves
local.properties
keystore.properties
secrets.properties
*.jks
*.keystore
*.p12
*.pfx
*.pem
*.key
google-services.json
.env
.env.*

# IDE
.idea/
*.iml
.vscode/
.project
.settings/
.classpath

# Logs, pacotes de correcao e lixo
*.log
log.txt
logcat*.txt
*.zip
*.tar.gz
*.tar
.DS_Store
Thumbs.db
*.swp
*~
'''
gi = p('.gitignore')
if not os.path.exists(gi):
    write(gi, GI.lstrip('\n'))
elif GI_MARK not in read(gi):
    with open(gi, 'a', encoding='utf-8', newline='\n') as f: f.write('\n' + GI)
    changed.append('.gitignore')

# ------------------------------------------------------------------ 11. README
README_MARK = '<!-- xaulinxs-readme -->'
README = README_MARK + r'''
<div align="center">

# 📧 XaulinXs Email

### O E-mail Original do AOSP, Independente do WebView do Sistema

**Sem depender do WebView do sistema. Sem Android Studio.**
**Apenas Gradle moderno, AGP atualizado e um WebView AOSP embutido nos assets.**

![Platform](https://img.shields.io/badge/Platform-Android-00E5FF?style=for-the-badge&labelColor=0D1117)
![AOSP](https://img.shields.io/badge/Based%20on-AOSP%20Email-1DE9B6?style=for-the-badge&labelColor=0D1117)
![WebView](https://img.shields.io/badge/WebView-AOSP%20Embutido-FF9100?style=for-the-badge&labelColor=0D1117)
![Gradle](https://img.shields.io/badge/Gradle-Moderno-00BCD4?style=for-the-badge&labelColor=0D1117)
![AGP](https://img.shields.io/badge/AGP-8.7.3-64FFDA?style=for-the-badge&labelColor=0D1117)
![API](https://img.shields.io/badge/API-21--34-00E5FF?style=for-the-badge&labelColor=0D1117)
![Security](https://img.shields.io/badge/WebView-Fail--Closed-FF5252?style=for-the-badge&labelColor=0D1117)
![License](https://img.shields.io/badge/License-Apache--2.0-1DE9B6?style=for-the-badge&labelColor=0D1117)

</div>

---

## 🚀 Por que este projeto existe

O app **Email** do **Android Open Source Project** (`com.android.email`) é um cliente de e-mail clássico, simples e sem telemetria. Com o tempo ele ficou difícil de compilar fora da árvore do AOSP e dependente de peças do sistema que mudam de aparelho para aparelho.

A mais crítica delas é o **WebView**: é ele que desenha cada mensagem HTML que chega na sua caixa de entrada — ou seja, renderiza conteúdo **não confiável** vindo da internet. Quando o app usa o WebView do sistema, a segurança e o comportamento do seu e-mail passam a depender de qual versão o fabricante (ou a Play Store) deixou instalada.

O **XaulinXs Email** nasce para resolver isso:

> Modernizar o build do Email do AOSP e **embutir o próprio WebView AOSP** dentro do app, carregado dinamicamente em tempo de execução (WebViewUpgrade). O motor que renderiza seus e-mails é o que **você** colocou na pasta `assets` — não o que o sistema tem.

---

## 🧭 Filosofia

- 🔒 **Independência** — o app não usa o WebView do sistema. Você escolhe a versão do motor.
- 🚫 **Fail-closed** — se o WebView embutido não carregar, o app **recusa** abrir mensagens HTML em vez de cair silenciosamente no WebView do sistema.
- 🧱 **Superfície mínima** — sem `allowBackup`, sem tráfego em texto claro, `PendingIntent` imutáveis, links de e-mail filtrados por esquema.
- 🎨 **Original, mas seu** — Material do AOSP preservado, com uma tela simples para trocar a cor de destaque e o fundo.
- 🛠️ **Simples de compilar** — Gradle puro, compila até no Termux.

---

## ✨ Destaques

- ✅ Código-fonte original do **Email + UnifiedEmail (AOSP)**
- ✅ **WebView AOSP embutido** em `assets/aosp_webview.apk`, aceita qualquer versão (nova ou antiga)
- ✅ Troca sincronizada em `Application.onCreate`, antes de qualquer WebView existir
- ✅ **Ponto único de criação de WebView** (`GatedWebView`) com padrões de segurança
- ✅ `onRenderProcessGone` tratado — renderizador morto não derruba o app
- ✅ **Rodapé nas configurações** com a versão do WebView AOSP em uso
- ✅ **Cores personalizáveis** (destaque e fundo) por editor hexadecimal
- ✅ Correções para **Android 12–14**: `FLAG_IMMUTABLE`, `RECEIVER_NOT_EXPORTED`, widget funcional
- ✅ Compatível de **Android 5.0 (API 21)** até **Android 14 (API 34)**

---

## 🏗 Estrutura do Projeto

```
Email/                      # módulo Gradle principal (app/)
UnifiedEmail/               # camada de UI unificada do AOSP
Email/app/src/main/assets/  # arquivos do WebView AOSP (.pak, .dat, .bin) + aosp_webview.apk (local)
Email/src/com/norman/webviewup/   # biblioteca WebViewUpgrade (vendorizada)
Email/src/com/android/email/webview/  # EmbeddedWebView e GatedWebView
Email/src/com/android/email/theme/    # cores personalizadas
```

---

## 📦 Como compilar

1. Copie o **APK do WebView AOSP** (qualquer versão) para:
   `Email/app/src/main/assets/aosp_webview.apk`
   *(ele está no `.gitignore`; não vai para o GitHub)*
2. Compile:

```bash
cd Email
gradle assembleDebug
```

Para trocar a versão do motor depois, basta **substituir o `aosp_webview.apk`** e recompilar — o app detecta a mudança e extrai o novo.

> ⚠️ Na **primeira abertura** (ou após trocar o APK) o app copia o arquivo para o armazenamento privado, o que pode levar alguns segundos.

---

## 🛡 Segurança

| Item                                            | Status |
|--------------------------------------------------|:------:|
| WebView do sistema desativado (fail-closed)      | ✅ |
| `allowBackup="false"`                            | ✅ |
| Tráfego em texto claro bloqueado                 | ✅ |
| `PendingIntent` com `FLAG_IMMUTABLE`             | ✅ |
| Acesso a arquivos/`content://` desligado nos e-mails | ✅ |
| Links de e-mail só `http(s)`, `mailto`, `tel`, `sms`, `geo` | ✅ |
| Renderizador morto não derruba o app             | ✅ |

---

## 🎨 Personalização

Em **Configurações → Cores** há um editor hexadecimal para a **cor de destaque** (laranja) e para o **fundo** (branco). A troca é feita em tempo de execução nas telas principais, no botão de escrever e no widget. A cor do texto não é ajustada automaticamente.

---

## 🎯 Compatibilidade

| Android    | Suporte |
|------------|:-------:|
| Android 5 – 11 | ✅ |
| Android 12 | ✅ |
| Android 13 | ✅ |
| Android 14 | ✅ |

---

## 📚 Créditos

- Android Open Source Project
- Google
- [WebViewUpgrade](https://github.com/JonaNorman/WebViewUpgrade) — troca de WebView em tempo de execução

---

## 📄 Licença

Apache License 2.0

O código-fonte original pertence ao Android Open Source Project.

---

<div align="center">

### ⭐ Se este projeto te ajudou, considere deixar uma estrela!

**E-mail do AOSP, com o motor de renderização nas suas mãos.**

</div>
'''
rd = p('README.md')
if (not os.path.exists(rd)) or README_MARK in read(rd) or len(read(rd)) < 300:
    write(rd, README)
else:
    log('  (README.md ja tem conteudo proprio; nao sobrescrevi)')

# ------------------------------------------------------------------ 12. untrack
if UNTRACK:
    try:
        out = subprocess.run(['git', 'ls-files', '-ci', '--exclude-standard', '-z'],
                             cwd=ROOT, capture_output=True, check=True).stdout
        files = [x for x in out.split(b'\0') if x]
        for i in range(0, len(files), 500):
            subprocess.run(['git', 'rm', '--cached', '-q', '--ignore-unmatch', '--'] + files[i:i+500],
                           cwd=ROOT, check=True)
        log('git: %d arquivos tirados do indice (continuam no disco).' % len(files))
    except Exception as e:
        log('  !! --untrack falhou: %s' % e)

log('')
log('Arquivos alterados/criados: %d' % len(changed))
for c in changed: log('  - ' + c)
if not changed: log('  (nada a fazer: ja estava tudo aplicado)')
if not os.path.exists(os.path.join(ASSETS_DST, 'aosp_webview.apk')):
    log('')
    log('LEMBRETE: copie o APK para Email/app/src/main/assets/aosp_webview.apk antes de compilar.')
