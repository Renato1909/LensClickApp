# Contrato de autenticação móvel — LensClick

Status: proposta técnica para implementação conjunta Web/API + Android.
Data: 2026-09-20

## 1. Objetivo

Definir o contrato para o aplicativo Android consumir o mesmo backend da plataforma web sem reutilizar a sessão de navegador.

A web atual usa sessão opaca armazenada no D1 e cookie `HttpOnly`, `SameSite=Lax` e `Secure` em HTTPS. O Android não deve depender desse cookie.

## 2. Regra principal

O Android terá uma sessão móvel revogável, distinta da sessão de navegador.

Não serão armazenados no Room:
- senha;
- hash de senha;
- token de acesso;
- credenciais em texto puro.

O segredo de sessão persistente será tratado posteriormente por armazenamento seguro apoiado pelo Android Keystore.

## 3. Endpoints web existentes

| Método | Endpoint | Uso |
|---|---|---|
| POST | `/api/auth/register` | cadastro |
| POST | `/api/auth/login` | login |
| GET | `/api/auth/me` | usuário autenticado |
| POST | `/api/auth/logout` | logout |
| POST | `/api/auth/password-reset/request` | solicitar recuperação |
| POST | `/api/auth/password-reset/confirm` | concluir recuperação |
| POST | `/api/auth/email/verify` | confirmar e-mail |
| POST | `/api/auth/email/resend` | reenviar confirmação |

O cadastro atual recebe `name`, `email`, `phone`, `password` e opcionalmente `language`. A senha exige pelo menos 8 caracteres, uma letra e um número.

O login atual recebe `email` e `password`, retorna `{ user }` e estabelece sessão por cookie.

## 4. Contrato móvel proposto

### 4.1 Login

`POST /api/mobile/auth/login`

Request:

```json
{
  "email": "usuario@example.com",
  "password": "senha",
  "deviceName": "Android",
  "appVersion": "1.0"
}
```

Resposta 200:

```json
{
  "user": {
    "id": 123,
    "name": "Nome",
    "email": "usuario@example.com",
    "phone": "..."
  },
  "session": {
    "accessToken": "<token-opaco>",
    "expiresAt": "2026-09-20T15:00:00Z"
  }
}
```

O token deve ser aleatório, opaco e revogável. O servidor deve persistir somente uma representação derivada/hash do token.

### 4.2 Sessão atual

`GET /api/mobile/auth/me`

Header:

`Authorization: Bearer <accessToken>`

Resposta 200:

```json
{
  "user": {
    "id": 123,
    "name": "Nome",
    "email": "usuario@example.com",
    "phone": "..."
  }
}
```

401 indica sessão ausente, expirada ou revogada.

### 4.3 Logout

`POST /api/mobile/auth/logout`

Header:

`Authorization: Bearer <accessToken>`

O backend revoga a sessão. O app remove o segredo do armazenamento seguro após logout e também deve limpar a sessão local diante de 401 definitivo.

### 4.4 Revogação

Troca de senha, exclusão da conta e eventos de segurança devem invalidar as sessões móveis conforme a política de revogação definida pelo backend.

## 5. Renovação

A primeira integração não introduzirá refresh token automaticamente.

Versão inicial:
- access token com expiração definida pelo backend;
- sessão revogável;
- novo login após expiração;
- logout explícito;
- revogação em troca de senha;
- armazenamento seguro no Android.

Refresh tokens rotativos podem ser avaliados posteriormente se forem necessários.

## 6. Erros HTTP

| HTTP | Significado | Comportamento Android |
|---|---|---|
| 200 | sucesso | atualizar estado |
| 201 | criado | atualizar estado |
| 202 | aceito/assíncrono | confirmação adequada |
| 400 | entrada inválida | erro de formulário |
| 401 | não autenticado | limpar sessão quando aplicável |
| 403 | não autorizado | acesso negado |
| 404 | não encontrado | recurso ausente |
| 409 | conflito | erro contextual |
| 429 | rate limit | aguardar, sem retry agressivo |
| 5xx | falha do servidor | erro transitório/retry controlado |

O Android não deve transformar mensagens textuais do servidor em regras de negócio. A implementação final deve definir códigos de erro estáveis.

## 7. Segurança

- HTTPS fora de ambientes locais.
- Token nunca em URL, logs, analytics, screenshots ou mensagens de erro.
- Não reutilizar cookie web como autenticação nativa.
- Não colocar credenciais no Room.
- Não duplicar hash de senha no Android.
- Não armazenar senha para login automático.
- Rate limiting continua no backend.
- O backend permanece como autoridade de autorização.

## 8. Separação no Android

**remote/auth/api/session**
- DTOs HTTP;
- cliente HTTP;
- Authorization;
- mapeamento de erros;
- sessão.

**data/local**
- Room;
- cache sem credenciais.

**domain**
- casos de uso e regras de apresentação, sem duplicar autorização do servidor.

UI/ViewModels não devem conhecer JSON bruto, headers HTTP ou detalhes do armazenamento do token.

## 9. Primeira fatia de produção

1. cadastro;
2. login;
3. restauração da sessão;
4. logout;
5. recuperação de senha;
6. verificação de e-mail;
7. expiração/revogação.

Depois: descoberta, perfis, portfólio, conversas e demais recursos.

## 10. Pendências antes do Retrofit

Este documento é uma proposta; **não altera o backend ainda**.

O backend precisa confirmar e versionar:
- rotas móveis definitivas;
- formato e expiração do token;
- estrutura de sessões móveis;
- política de revogação;
- códigos de erro estáveis;
- limites de rate limiting;
- compatibilidade entre versão do app e API.

Após essa confirmação, o contrato deverá ter testes de API antes da implementação de produção no Android.

## 11. Critério de aceite

O contrato estará aprovado quando:
- uma sessão Android puder ser revogada pelo servidor;
- senha, hash e token não forem persistidos no Room;
- logout e troca de senha invalidarem sessões conforme a política;
- 401 tiver tratamento determinístico;
- testes cobrirem sucesso, entrada inválida, credencial inválida, rate limit e revogação;
- web e Android utilizarem o mesmo usuário e as mesmas regras de negócio.
