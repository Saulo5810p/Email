# SECURITY.md — AOSP Email Revivido (com.android.email)

## Threat model

O app processa e-mail de origem não confiável por definição: HTML, CSS,
URLs, imagens, anexos, headers e MIME de um e-mail recebido podem ser
controlados integralmente por um atacante. O modelo de ameaça assume:

- O servidor IMAP/POP3/SMTP/EAS configurado pode ser malicioso ou
  comprometido (man-in-the-middle na rede).
- O conteúdo de qualquer e-mail recebido é hostil até prova em contrário.
- Outros apps instalados no dispositivo podem tentar acessar dados do
  Email via providers, intents ou anexos compartilhados.
- O dispositivo não tem privilégio de sistema — o app roda como app normal.

## Decisões de segurança

- **Sem GMS, sem Google Play Services, sem Gmail proprietário.** Nenhuma
  dependência desse tipo foi introduzida.
- **Sem API privada / `@hide` / reflection para contornar permissões /
  platform certificate / sharedUserId.** O app não assume privilégio de
  sistema em nenhum ponto novo introduzido nesta migração.

## Política de TLS

Ver `AUDITORIA_REDE.md` para o detalhamento completo. Resumo: validação de
certificado via cadeia de confiança do sistema por padrão; modo
"autoassinado" é opt-in por conta com TOFU de chave pública (não
verificação global desligada). Nenhum downgrade automático TLS→plaintext.

## Política de certificado

Sem "ignorar certificado" automático em nenhum caminho de código. O único
bypass existente (`SameCertificateCheckingTrustManager`) é por conta,
explícito, e documentado em `AUDITORIA_REDE.md` como ponto de revisão de UI.

## Política de HTML

O sanitizador OWASP HTML Sanitizer (`owasp-java-html-sanitizer`) permanece
como dependência — não foi removido nem substituído. Esquemas de URL
perigosos (`javascript:`, `data:`, `file:`, `content:`, `intent:`,
`android-app:`) ainda precisam de auditoria dedicada dentro do código de
renderização de mensagem — **não coberta nesta entrega** (é trabalho de
M12 "corrigir WebView/HTML" no roteiro de marcos, depois do primeiro
build).

## Política de anexos

`AttachmentProvider` exige `READ_ATTACHMENT` (agora `signature`, era
`dangerous` — ver mudança no manifest). Uso de `content://` via provider já
é o padrão original do AOSP aqui, não `file://` direto — não precisou de
correção nesse ponto específico. Auditoria de path traversal em nomes de
anexo fornecidos pelo remetente **não coberta nesta entrega**.

## Política de providers

- `EmailProvider`: mantido `exported="true"` com `permission=
  "com.android.email.permission.ACCESS_PROVIDER"` (protectionLevel
  signature) — necessário porque sync adapters/authenticators do sistema
  precisam acessá-lo. Comentário original de aviso ("expõe senhas e
  informações confidenciais") preservado no manifest.
- `AttachmentProvider`: `exported="true"` necessário (compartilha anexos
  com apps de terceiros via `content://`), protegido por
  `READ_ATTACHMENT` agora `signature`.
- `EmailConversationProvider`: mudado de `exported="true"` para
  `exported="false"` — não encontrei consumidor externo legítimo.
- `EmailAccountCacheProvider`, `EmlAttachmentProvider`: já eram
  `exported="false"` no original, mantidos.

Auditoria de SQL injection via `selection`/`sortOrder`/projection nas
queries do `EmailProvider` **não coberta nesta entrega**.

## Política de backup

`android:allowBackup="false"` já estava correto no manifest original e foi
mantido — dados de conta/credenciais não vão para backup automático.

## Política de logging

Não auditado nesta entrega. `LOG_ENABLED = false` como padrão em
`SSLUtils` foi observado e é o comportamento correto (evita vazar detalhes
de handshake em log); não verifiquei os demais pontos de logging do app.

## Política de dependências

Ver `app/build.gradle` para o mapeamento completo `Android.mk` →
Maven/AndroidX. Módulos AOSP sem artefato publicado listados em
`VENDORIZAR.md`, ainda não trazidos.

## Known limitations (estado desta entrega)

- Projeto ainda **não compilou** (nenhum ambiente com SDK/AGP disponível
  nesta etapa de preparo) — ver `TOOLCHAIN.md`.
- 4 módulos AOSP (`chips`, `photoviewer`, `bitmap`, `android-common`) ainda
  não vendorizados — build vai falhar nas classes que os importam até isso
  ser feito.
- Migração de `org.apache.http.legacy` para API moderna **não realizada**
  ainda, apenas mapeada (`AUDITORIA_REDE.md`).
- Sanitização de HTML/WebView, path traversal de anexos, e SQL injection em
  providers **não auditados** nesta entrega — permanecem como trabalho
  pendente de M11-M13 do roteiro.
