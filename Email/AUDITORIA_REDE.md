# Auditoria de rede / TLS

## org.apache.http.legacy — inventário completo

Busquei todo uso de `org.apache.http.{client,conn,params,impl,HttpEntity,
HttpResponse,HttpRequest}` (excluindo Apache MIME4J, que é parser de e-mail,
não cliente HTTP) em `Email/` + `UnifiedEmail/`. Resultado: **3 arquivos**,
uso real e contido — não é a base inteira do transporte de e-mail, que já
usa sockets TLS diretos via `SSLUtils`/`MailTransport`, e não `HttpClient`.

| Arquivo | Refs | Papel |
|---|---|---|
| `emailcommon/utility/EmailClientConnectionManager.java` | 5 | Connection manager do HttpClient legado |
| `emailcommon/utility/SSLSocketFactory.java` | 11 | Wrapper de socket factory ligado ao HttpClient |
| `provider_src/.../mail/internet/OAuthAuthenticator.java` | 10 | Faz POST para token/refresh endpoint OAuth via `DefaultHttpClient` |

**Status: REVIEW REQUIRED.** Migração recomendada: substituir
`OAuthAuthenticator` por `HttpURLConnection` nativo (a chamada é um único
POST com corpo `application/x-www-form-urlencoded` — não precisa de
biblioteca externa) e remover `EmailClientConnectionManager`/
`SSLSocketFactory` (a variante ligada ao Apache HttpClient) já que o
transporte de e-mail (IMAP/POP3/SMTP) passa por `MailTransport`, que usa
`javax.net.ssl.SSLSocketFactory` puro, não Apache HttpClient. Essa migração
ainda não foi feita nesta entrega — é trabalho de M11 ("modernizar
rede/TLS") no roteiro de marcos, depois do primeiro build funcional (M3).

## TLS / certificados — o que já existe e está correto

- `MailTransport.java` usa `HttpsURLConnection.getDefaultHostnameVerifier()`
  (verificador de hostname padrão do sistema) — correto, não substituído
  por implementação permissiva.
- `SSLUtils.getSSLSocketFactory(...)` no modo seguro (`insecure=false`) usa
  a cadeia de confiança padrão do sistema (`SSLSocketFactoryWrapper.getDefault`).
  Não encontrei `TrustManager` que aceite qualquer certificado
  incondicionalmente.

## TLS — ponto que exige decisão do usuário, não do código (REVIEW REQUIRED)

Quando `insecure=true` (fluxo explícito de "aceitar certificado
autoassinado" por conta), `SSLUtils.SameCertificateCheckingTrustManager`
implementa **trust-on-first-use (TOFU)**: na primeira conexão, salva a
chave pública do certificado do servidor; em conexões seguintes, só
verifica se a chave pública mudou. Isso **não valida hostname nem cadeia de
CA** nesse modo — é esperado, porque é justamente o modo para servidores
self-signed/internos.

Isso já está alinhado com o que o briefing pede na seção 12 ("projete uma
confirmação explícita do usuário com hostname/porta/fingerprint"), mas a
parte de UI que mostra esse fingerprint ao usuário antes de ativar o modo
`insecure` não foi auditada nesta entrega — verifique se a tela de
configuração de conta realmente exibe fingerprint antes de permitir marcar
a conta como "aceitar certificado inválido". Não modernizei esse
comportamento; documento aqui para revisão explícita.

## Cleartext

- `AndroidManifest.xml`: `usesCleartextTraffic` mudado de `true` para
  `false`.
- `app/src/main/res/xml/network_security_config.xml` criado com
  `cleartextTrafficPermitted="false"` no `base-config`, sem exceções de
  domínio.

## STARTTLS

Não tratado como equivalente a conexão insegura em nenhum ponto que
encontrei — o `MailTransport` distingue conexão TLS direta de STARTTLS e
ambos terminam validando o certificado da mesma forma via `SSLUtils`.
