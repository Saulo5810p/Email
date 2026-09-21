# AOSP Email Revivido — com.android.email

Port do app AOSP Email (`Email/` + `UnifiedEmail/`) para build Gradle
moderno, compilável no Termux, gerando um único APK `com.android.email`,
sem GMS/Play Services/Gmail proprietário.

## Estado atual (M1/M2 em andamento — ainda não chegou em M3)

- [x] **M0 — inventário de source**: concluído. Ver `ARQUITETURA.md`.
- [x] **M1 — esqueleto Gradle**: concluído (`settings.gradle`,
      `build.gradle`, `gradle.properties`, `app/build.gradle`).
- [~] **M2 — source integrado**: source sets configurados e mapeados
      1:1 a partir do `Android.mk` original; **4 módulos AOSP ainda
      pendentes de vendorização** (ver `VENDORIZAR.md`) — o build vai
      falhar nas classes que os importam até isso ser feito.
- [ ] **M3 — APK debug builda**: ainda não alcançado. Não há ambiente
      com Android SDK/AGP disponível na etapa de preparo onde este
      projeto foi montado — o primeiro `assembleDebug` real precisa
      rodar no seu Termux.
- [ ] M4–M8: pendentes, dependem de M3.

## Toolchain

Ver `TOOLCHAIN.md` para o detalhe completo, incluindo o teste mínimo que
você precisa rodar (`./gradlew --version`) porque **Gradle 9.6.1 + AGP
8.7.x não é uma combinação oficialmente suportada** segundo a doc atual do
AGP (AGP 8.7.x pede Gradle 8.9+, não 9.6.1).

| | |
|---|---|
| compileSdk | 34 |
| targetSdk | 34 |
| minSdk | 21 |
| Java | 17 (source/target compatibility) |
| AGP | 8.7.3 (fixado em `build.gradle` raiz) |
| Gradle | verificar com `./gradlew --version` — ver `TOOLCHAIN.md` |
| AAPT2 | nativo do Termux, path a preencher em `gradle.properties` |

## Arquivos criados

```
settings.gradle
build.gradle
gradle.properties
app/build.gradle
app/src/main/AndroidManifest.xml   (cópia hardened de Email/AndroidManifest.xml)
app/src/main/res/xml/network_security_config.xml
TOOLCHAIN.md
ARQUITETURA.md
VENDORIZAR.md
AUDITORIA_REDE.md
SECURITY.md
SECURITY_AUDIT.md
```

## Arquivos modificados

Nenhum arquivo dentro de `Email/` ou `UnifiedEmail/` (as árvores fonte
clonadas do repositório) foi modificado — todo o hardening de segurança
foi aplicado na cópia do manifest dentro de `app/src/main/`, preservando os
snapshots AOSP originais intocados como referência.

## Arquivos removidos

Nenhum.

## Dependências adicionadas

androidx.core, androidx.media, androidx.fragment, androidx.appcompat,
androidx.gridlayout, androidx.legacy (core-utils/core-ui/v13),
androidx.annotation, guava, owasp-java-html-sanitizer, androidx.test
(runner/rules). Ver `app/build.gradle` para versões exatas e o
mapeamento completo a partir de cada `LOCAL_STATIC_*_LIBRARIES` do
`Android.mk` original.

## Dependências removidas

`android-opt-datetimepicker` (zero uso real no código, confirmado por
busca — ver `SECURITY_AUDIT.md` SEC-008). `org.apache.http.legacy` marcado
para remoção mas ainda não migrado (ver `AUDITORIA_REDE.md`).

## Problemas AOSP encontrados

Ver `SECURITY_AUDIT.md` — 12 findings catalogados, dos quais 5 já
corrigidos nesta entrega e 7 marcados `REVIEW REQUIRED` para as próximas
rodadas (M11-M13 do roteiro).

## Auditoria de segurança

`SECURITY.md` (políticas) + `SECURITY_AUDIT.md` (tabela de findings).

## Comandos para compilar no Termux

```bash
# 1. Verifique a combinação Gradle/AGP real disponível
./gradlew --version

# 2. Detecte o AAPT2 do Termux e preencha gradle.properties
command -v aapt2 && aapt2 version
# edite gradle.properties, descomente e ajuste:
#   android.aapt2FromMavenOverride=/caminho/real/aapt2

# 3. Traga os módulos AOSP pendentes (obrigatório antes do build —
#    ver VENDORIZAR.md para a lista exata e como trazer)

# 4. Build debug
./gradlew :app:assembleDebug

# 5. Instalar direto no dispositivo (opcional, com o app já rodando local)
./gradlew :app:installDebug
```

## Caminho do APK

```
app/build/outputs/apk/debug/app-debug.apk      (após assembleDebug)
app/build/outputs/apk/release/app-release.apk  (após assembleRelease, precisa de signingConfig)
```

## Próximos passos reais, na ordem

1. Vendorizar os 4 módulos AOSP pendentes (`VENDORIZAR.md`) — é o que
   bloqueia M3.
2. Primeiro `assembleDebug` no Termux, corrigir os erros de compilação que
   aparecerem (normal e esperado — nenhum ambiente aqui pôde validar isso
   antes).
3. Instalar e testar (M4).
4. Retomar o roteiro de segurança nos itens `REVIEW REQUIRED` de
   `SECURITY_AUDIT.md`: sanitização de HTML/WebView (SEC-009), SQL
   injection em providers (SEC-010), path traversal em anexos (SEC-011),
   migração de `org.apache.http.legacy` (SEC-006/007).
