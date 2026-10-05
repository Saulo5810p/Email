<!-- xaulinxs-readme -->
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
