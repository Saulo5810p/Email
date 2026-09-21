# ARQUITETURA.md

## O que o `Android.mk` original realmente diz

`Email/Android.mk` (target que gera o APK final):

```make
unified_email_dir := ../UnifiedEmail
LOCAL_SRC_FILES := $(call all-java-files-under, $(unified_email_dir)/src)
LOCAL_SRC_FILES += $(call all-java-files-under, src/com/android)
LOCAL_SRC_FILES += $(call all-java-files-under, provider_src/com/android)
LOCAL_SRC_FILES += $(call all-java-files-under, src/com/beetstra)
LOCAL_RESOURCE_DIR := res  ../UnifiedEmail/res
LOCAL_ASSET_DIR := ../UnifiedEmail/assets
```

`Email/emailcommon/Android.mk` (static library separada, incorporada ao
mesmo APK):

```make
LOCAL_SRC_FILES := $(call all-java-files-under, src/com/android/emailcommon)
LOCAL_SRC_FILES += <3 arquivos específicos de ../../UnifiedEmail/src>
LOCAL_SRC_FILES += $(call all-java-files-under, ../../UnifiedEmail/src/org)
LOCAL_SRC_FILES += $(call all-java-files-under, ../../UnifiedEmail/src/com/android/emailcommon)
```

Note que **`UnifiedEmail/unified_src` nunca aparece em nenhum dos dois**.
Confirmado por leitura direta do repositório clonado — não presumi isso, é
o que os dois `Android.mk` dizem e o que existe fisicamente no repo.

## Por que o Gradle não funde fisicamente as árvores

O AGP resolve múltiplos `java.srcDirs`/`res.srcDirs` nativamente, com merge
de resources automático (o mesmo mecanismo que o `res_dir := res
$(unified_email_dir)/res` do Make já delegava ao AAPT). Copiar arquivos
fisicamente para uma pasta comum seria regressão em relação ao próprio
Make original, que já mantinha as árvores fisicamente separadas.

`app/build.gradle` reproduz isso com:

```groovy
java.srcDirs(
    "../UnifiedEmail/src",
    "../Email/src/com/android",
    "../Email/provider_src/com/android",
    "../Email/src/com/beetstra"
)
java.srcDir("../Email/emailcommon/src/com/android/emailcommon")
res.srcDirs("../Email/res", "../Email/emailcommon/res", "../UnifiedEmail/res")
assets.srcDirs("../UnifiedEmail/assets")
```

`emailcommon` deixou de ser uma static library Gradle separada e virou mais
um `srcDir` dentro do mesmo módulo `app` — a razão é que o objetivo final é
**um único APK**, e como nada mais no projeto consome `emailcommon` como
biblioteca reutilizável fora deste app, um módulo Gradle extra só
adicionaria complexidade de build sem benefício real. Se no futuro isso
precisar virar módulo separado (por exemplo pra reuso em outro app da
XaulinXs Foundry), é uma refatoração direta.

## Duplicatas de nome de arquivo — verificação, não suposição

4 nomes de arquivo aparecem em mais de uma árvore fonte:

| Arquivo | Localização 1 | Localização 2 | Colisão real? |
|---|---|---|---|
| `WidgetProvider.java` | `Email/provider_src/.../email/provider/` | `UnifiedEmail/src/.../mail/widget/` | Não — pacotes `com.android.email.provider` vs `com.android.mail.widget` |
| `Account.java` | `Email/emailcommon/.../emailcommon/provider/` | `UnifiedEmail/src/.../mail/providers/` | Não — pacotes `com.android.emailcommon.provider` vs `com.android.mail.providers` |
| `CountingOutputStream.java` | `Email/emailcommon/.../emailcommon/utility/` | `UnifiedEmail/src/org/apache/commons/io/output/` | Não — pacotes distintos |
| `Mailbox.java` | `Email/emailcommon/.../emailcommon/provider/` | `UnifiedEmail/src/org/apache/james/mime4j/field/address/` | Não — pacotes distintos |

Verificado via `find` + inspeção do caminho completo de cada arquivo, não
apenas o basename. Nenhum dos quatro exige resolução de conflito — o
compilador Java distingue por pacote normalmente. Documentado em
`SECURITY_AUDIT.md` (SEC-012) como ponto a reconferir depois que os módulos
vendorizados (`VENDORIZAR.md`) forem trazidos, caso eles introduzam alguma
colisão nova.

## Resource dirs — o que entrou e por quê

`Email/res`, `Email/emailcommon/res`, `UnifiedEmail/res` — os três dirs que
o `Android.mk` original referenciava (`res_dir := res
$(unified_email_dir)/res` no target Email, `LOCAL_RESOURCE_DIR :=
$(LOCAL_PATH)/res` no target emailcommon). Nenhum diretório de resource foi
adicionado além desses três.

## Assets — só UnifiedEmail, propositalmente

O `Android.mk` original tem o comentário explícito:

```
# Use assets dir from UnifiedEmail
# (the default package target doesn't seem to deal with multiple asset dirs)
```

Ou seja, mesmo o build original só empacotava assets de `UnifiedEmail/`,
não de `Email/assets` (que existe fisicamente no repo com `loading.gif`,
`loading.html`, `test.html`, mas nunca foi declarado como
`LOCAL_ASSET_DIR`). Preservei essa mesma decisão em
`assets.srcDirs("../UnifiedEmail/assets")` — não adicionei `Email/assets`
por conta própria, porque isso mudaria o comportamento do app em relação
ao original sem necessidade técnica comprovada. Se `Email/assets/test.html`
for necessário para algo, é uma decisão para revisar depois com evidência
concreta de que falta.
