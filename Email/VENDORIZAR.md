# Módulos AOSP a vendorizar

O `Email/Android.mk` original depende de 5 módulos `frameworks/opt/*` /
`packages/apps/*` que **não existem** no repositório `Saulo5810p/Email` nem
têm artefato publicado no Maven Central. Não são stubs — são bibliotecas
AOSP reais, Apache 2.0, que precisam ter o código-fonte trazido para dentro
deste projeto (vendorizado) porque não há outra forma de obtê-las.

Não criei classes vazias fingindo implementar essas APIs — isso violaria a
regra 49 do briefing ("não criar stubs vazios para fingir que uma API
funciona"). Até esses módulos serem trazidos, o build vai falhar exatamente
nos pontos que os importam, e isso é o comportamento correto e esperado.

## 1. `libchips` → `com.android.ex.chips.*`

Fonte: `android.googlesource.com/platform/frameworks/opt/chips`

Classes usadas neste projeto (3):
- `BaseRecipientAdapter`
- `DropdownChipLayouter`
- `RecipientEditTextView`

Usadas para o campo de destinatários (To/Cc/Bcc) com chips visuais e
autocomplete de contatos.

## 2. `libphotoviewer_appcompat` → `com.android.ex.photo.*`

Fonte: `android.googlesource.com/platform/frameworks/opt/photoviewer`

Classes usadas (10):
- `ActionBarInterface`, `Intents`, `PhotoViewActivity`, `PhotoViewController`
- `fragments.PhotoViewFragment`
- `provider.PhotoContract`
- `util.Exif`, `util.ImageUtils`, `util.Trace`
- `views.ProgressBarWrapper`

Usadas para visualizar fotos anexadas em tela cheia.

## 3. `android-opt-bitmap` → `com.android.bitmap.*`

Fonte: `android.googlesource.com/platform/frameworks/opt/bitmap`

Classes usadas (6):
- `BitmapCache`, `DecodeTask`, `RequestKey`, `ReusableBitmap`,
  `UnrefedBitmapCache`
- `util.Trace`

Cache de bitmap para thumbnails/avatares na lista de mensagens.

## 4. `android-common` → `com.android.common.*`

Fonte: `android.googlesource.com/platform/packages/apps/UnifiedEmail`
(o módulo `android-common` é compartilhado entre vários apps AOSP; o
subconjunto usado aqui é pequeno)

Classes usadas (3):
- `Rfc822Validator`
- `contacts.DataUsageStatUpdater`
- `content.ProjectionMap`

## 5. `android-opt-datetimepicker`

**Não usado.** Busquei `import com.android.datetimepicker` em todo o
source (`Email/` + `UnifiedEmail/`) e não há nenhuma ocorrência. Pode ser
removido da lista de dependências do `Android.mk` original sem qualquer
impacto — provavelmente era usado por um caminho de código já removido em
versões anteriores do UnifiedEmail.

## Como trazer

1. No Termux, com rede completa (este sandbox de preparo não tem acesso a
   `android.googlesource.com`), clone cada módulo via Gitiles/git:
   ```
   git clone https://android.googlesource.com/platform/frameworks/opt/chips
   git clone https://android.googlesource.com/platform/frameworks/opt/photoviewer
   git clone https://android.googlesource.com/platform/frameworks/opt/bitmap
   ```
2. Copie apenas `src/` (e `res/` quando existir) de cada um para dentro
   deste projeto, por exemplo em `vendor/chips/`, `vendor/photoviewer/`,
   `vendor/bitmap/`.
3. Adicione um `sourceSets` extra em `app/build.gradle` apontando pra essas
   pastas (mesmo padrão já usado para `Email/`/`UnifiedEmail/`), OU crie
   módulos Gradle separados (`:vendor-chips` etc.) se preferir isolar —
   qualquer uma das duas abordagens é compatível com a arquitetura de APK
   único já montada.
4. Adapte cada módulo aos imports `androidx.*` modernos (esses módulos AOSP
   antigos ainda podem referenciar `android.support.*` legado — normalize
   para `androidx`).
5. Preserve o `NOTICE`/licença de cada módulo trazido (Apache 2.0) junto do
   `NOTICE` deste projeto.

Isso é o próximo passo real de M2 (source integrado) antes do primeiro
`assembleDebug`.
