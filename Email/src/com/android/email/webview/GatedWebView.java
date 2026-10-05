package com.android.email.webview;

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
