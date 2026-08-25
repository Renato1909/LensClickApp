# LensClick — Contrato da API (resumo para o app)

Base URL de produção: `https://lensclick.renato-aparecido-business.workers.dev`

## Regras gerais

- **Autenticação:** cookie de sessão (`Set-Cookie` no login/registro). O app precisa usar um CookieJar persistente.
- **CORS:** o worker NÃO envia cabeçalhos CORS e bloqueia requisições não-GET sem `Origin === request.origin` (`requestOriginIsAllowed`). Um app Android nativo (OkHttp/Retrofit) **não envia Origin**, então passa direto — sem alteração no backend para GET/POST/PUT/DELETE nativos.
- **Erros:** JSON `{ "error": "mensagem em PT" }` com status 400/401/403/404/409/429/503.
- **Rate limit:** 429 em register (8/15min), login (10/15min), uploads de portfólio (20/h).
- Senha válida: mínimo 8 caracteres, ao menos 1 letra e 1 número.
- Especialidades válidas: `wedding, events, portrait, family, corporate, product, fashion, newborn, nature, real-estate` (máx. 6 por perfil).

## Autenticação

| Rota | Método | Body | Resposta |
|---|---|---|---|
| `/api/auth/register` | POST | `{name≤100, email, phone≤30, password}` | 201 `{user}` + cookie |
| `/api/auth/login` | POST | `{email, password}` | 200 `{user}` + cookie; 401 erro |
| `/api/auth/logout` | POST | — | 200 |
| `/api/auth/me` | GET | — | 200 `{user}` ou 401 |
| `/api/auth/password-reset/request` | POST | `{email, language:"pt"\|"en"}` | 202 genérico (requer Brevo) |
| `/api/auth/password-reset/confirm` | POST | `{token, newPassword}` | 200 |

## User shape (`publicUser`)

```json
{ "id":1, "name":"", "email":"", "phone":"", "role":"client|photographer",
  "age":null, "gender":"male|female|other",
  "profilePic":"/api/profile-picture/{id}?v=..." }
```

## Busca pública

- `GET /api/photographers?city=&state=UF&specialty=&availability=available|limited|unavailable&page=&pageSize=` → `{ photographers: [...], total, page, pageSize }`
- Cada item = perfil profissional público + `name`.
- Detalhe: `GET /api/photographers/{slug}` → perfil completo + portfólio (`portfolio[].imageUrl`).
- Imagens públicas: `/api/profile-picture/{id}?v=` e `/api/photographers/{slug}/portfolio/{itemId}?v=`

## Perfil profissional (`professionalProfile`)

Campos: `slug, bio(60..1200 p/ publicar), specialties[], city, state(2 letras), serviceRadiusKm(0..1000), startingPriceCents(0..100000000), availability, publicEmail, publicPhone, instagram(regex ^[A-Za-z0-9._]{1,30}$), website, showEmail, showPhone, isPublished`.

- `GET/PUT /api/account/professional-profile` (PUT exige os campos acima; publicar requer foto+bio 60++especialidade+cidade+contato público)

## Conta autenticada

- `PUT /api/profile` → `{name,email,phone,role,gender,age,profilePic?,currentPassword?}` (trocar e-mail exige senha atual)
- `PUT /api/account/password` → `{currentPassword,newPassword}`
- `POST multipart /api/account/photo` (campo `profilePicture`, ≤450 KB, png/jpg/webp/gif)
- `DELETE /api/account/photo`
- `DELETE /api/auth/delete-account` → body `{password}`
- Portfólio (só fotógrafo, máx. 12):
  - `POST multipart /api/account/portfolio` (campo `portfolioImage`, ≤4 MB) → 201 `{item}`
  - `PUT /api/account/portfolio` → `{items:[{id,altText?,caption?}]}` (reordenar/editar)
  - `DELETE /api/account/portfolio/{id}`
  - Imagem própria: `GET /api/account/portfolio/{id}/image?v=`

## Novidades a criar (backend novo, repo privado LensClick)

### Orçamentos
Tabela `quote_requests`: id, client_id, photographer_id(user_id do fotógrafo), message, event_date, budget_cents, status(`pending|accepted|declined`), timestamps.
Rotas planejadas:
- `POST /api/quotes` (cliente) `{photographerId, message, eventDate?, budgetCents?}`
- `GET /api/quotes` (recebidas do fotógrafo / enviadas do cliente via `?box=sent|received`)
- `PUT /api/quotes/{id}` (fotógrafo) `{status}`

### Mensagens
Tabelas `conversations` (client_id, photographer_id, unique par) e `messages` (conversation_id, sender_id, body, created_at, read_at).
Rotas planejadas:
- `GET /api/messages/conversations`
- `POST /api/messages/conversations` `{peerUserId}` (cria se não existe)
- `GET /api/messages/conversations/{id}/messages?page=`
- `POST /api/messages/conversations/{id}/messages` `{body}`
