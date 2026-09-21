# Toolchain

## Combinação solicitada vs. combinação oficialmente suportada

Você pediu Gradle 9.6.1 + AGP 8.7.x. Segundo a documentação oficial do AGP,
**AGP 8.7.x é suportado a partir de Gradle 8.9**, não de Gradle 9.6.1.
Gradle 9.x é a linha associada a AGP 8.9+/8.10+, não 8.7.x.

**Gradle 9.6.1 + AGP 8.7.x → combinação não suportada oficialmente.**

Não fiz nenhum hack para forçar essa combinação. O `build.gradle` raiz fixa
`com.android.application` em `8.7.3`. Rode o teste mínimo abaixo no seu
Termux antes de mais nada:

```bash
./gradlew --version
```

- Se o Gradle instalado for 8.9–8.13: siga normalmente, é a combinação
  suportada com AGP 8.7.3.
- Se só tiver Gradle 9.6.1 disponível no Termux: suba o AGP para uma versão
  8.9+/8.10+ compatível com Gradle 9.6.1 (troque a versão no `build.gradle`
  raiz) — não modifique classes internas do AGP/Gradle para forçar 8.7.x.

## compileSdk / targetSdk / minSdk

Conforme pedido (sem "neurose de subir pra 21" em target/compile):

- `compileSdk = 34`
- `targetSdk = 34`
- `minSdk = 21`

O `Android.mk` original tinha `targetSdkVersion="24"` / `minSdkVersion="14"`.
`minSdk 21` é o piso real deste build — não dá pra ir mais baixo sem reverter
androidx.appcompat/fragment modernos, que já exigem API 21 (Lollipop) como
mínimo absoluto.

`compileSdk 35` fica como upgrade possível mais tarde, mas só depois que o
AAPT2 do Termux for confirmado compatível — não force API mais alta só pelo
número.

## AAPT2 nativo do Termux

Ver instruções e mecanismo (`android.aapt2FromMavenOverride`) em
`gradle.properties`. Precisa ser preenchido por você, rodando no
dispositivo, porque o `command -v aapt2` só faz sentido no ambiente real do
Termux — este sandbox de preparo não tem Termux nem AAPT2 instalado.

## Java

Este projeto usa `sourceCompatibility`/`targetCompatibility = 17`. O sandbox
onde este projeto foi montado tem OpenJDK 21 disponível; o Termux também
deve ter Java 17+ disponível via `pkg install openjdk-17`.

## O que NÃO foi validado nesta entrega

Este ambiente de preparo não tem Android SDK, AGP ou acesso de rede a
`dl.google.com` (onde ficam os artefatos do AGP e do androidx). Ou seja:
**o `gradle assembleDebug` real ainda não rodou** — o que está pronto é a
configuração Gradle, os source sets mapeados a partir do `Android.mk`
original, e o manifest com hardening aplicado. O primeiro `assembleDebug`
de verdade precisa acontecer no seu Termux, e é esperado que apareçam erros
de compilação nessa primeira rodada (principalmente por causa dos módulos
pendentes listados em `VENDORIZAR.md`) — isso é o M3 do roteiro, ainda não
alcançado.
