# Proposta de contrato — orçamentos, propostas, agenda e favoritos

Status: **APROVADO pelo mantenedor (2026-11-14).** Auditoria (`budget_audit`) incluída na primeira fatia; limite de 1 pedido pendente por par cliente/fotógrafo. Novo contrato no backend `LensClick` + consumo Android. Escopo completo da Feira Tecnológica (ver [`DECISOES.md`](./DECISOES.md)).
Data: 2026-11-14

- Backend de referência: `Renato1909/LensClick` @ `6659948` (padrões `review_requests`, `conversations`/`messages`, `review_audit`).
- Android: protótipo local atual (`QuoteScreen`, `Budget`, `BudgetsScreen`, `ProRequests`, `ProAgenda`, favoritos) a ser substituído por dados remotos.
- Contrato base: [`CONTRATO_AUTENTICACAO_MOBILE.md`](./CONTRATO_AUTENTICACAO_MOBILE.md) e [`PROPOSTA_CONTRATO_MOVEL_PERFIL_PORTFOLIO.md`](./PROPOSTA_CONTRATO_MOVEL_PERFIL_PORTFOLIO.md).

## 1. Objetivo e princípio

Permitir que o cliente solicite um orçamento a um fotógrafo, receba uma proposta e a aceite/recuse, e que o fotógrafo gerencie pedidos e agenda. Favoritos permitem ao cliente salvar fotógrafos.

**Princípio:** a API do `LensClick` é a fonte de verdade. O Android não cria regra de negócio paralela nem banco local de orçamentos. As rotas de conta aceitam `Authorization: Bearer` (como perfil/portfólio), sem endpoints móveis separados.

## 2. Modelo de dados (novas migrações)

Seguir o padrão D1 das migrações existentes (FK `ON DELETE CASCADE`, `CHECK`, índices, `created_at`/`updated_at`).

### 2.1 `budget_requests`
| Coluna | Tipo / regra |
|---|---|
| `id` | INTEGER PK AUTOINCREMENT |
| `client_user_id` | INTEGER NOT NULL, FK users |
| `photographer_user_id` | INTEGER NOT NULL, FK users |
| `event_type` | TEXT NOT NULL, `length BETWEEN 2 AND 80` |
| `event_date` | TEXT NOT NULL (data ISO `YYYY-MM-DD`) |
| `place` | TEXT NOT NULL, `length BETWEEN 2 AND 160` |
| `duration` | TEXT, `length <= 80` |
| `description` | TEXT NOT NULL, `length BETWEEN 10 AND 1000` |
| `additional_info` | TEXT, `length <= 1000` |
| `status` | TEXT NOT NULL DEFAULT 'pending', `CHECK IN ('pending','proposed','accepted','rejected','cancelled')` |
| `created_at`/`updated_at` | TEXT DEFAULT CURRENT_TIMESTAMP |

`CHECK (client_user_id <> photographer_user_id)`. Índices por `(photographer_user_id, status, created_at DESC)` e `(client_user_id, created_at DESC)`. Limite de **1 pedido pendente por par** cliente/fotógrafo (índice único parcial `WHERE status='pending'`, como `review_requests`).

### 2.2 `proposals`
| Coluna | Tipo / regra |
|---|---|
| `id` | INTEGER PK AUTOINCREMENT |
| `request_id` | INTEGER NOT NULL, FK budget_requests |
| `photographer_user_id` | INTEGER NOT NULL, FK users |
| `price_cents` | INTEGER NOT NULL, `CHECK 0..100000000` |
| `conditions` | TEXT, `length <= 1000` |
| `status` | TEXT NOT NULL DEFAULT 'pending', `CHECK IN ('pending','accepted','rejected','withdrawn')` |
| `created_at`/`updated_at` | TEXT |

Índice único parcial: uma proposta `pending` por `request_id`. Ao aceitar uma proposta, o `budget_request` vira `accepted` e as demais propostas do pedido viram `rejected`.

### 2.3 `favorites`
| Coluna | Tipo / regra |
|---|---|
| `id` | INTEGER PK AUTOINCREMENT |
| `client_user_id` | INTEGER NOT NULL, FK users |
| `photographer_user_id` | INTEGER NOT NULL, FK users |
| `created_at` | TEXT |

`UNIQUE (client_user_id, photographer_user_id)`, `CHECK client<>photographer`, FKs cascade.

### 2.4 Agenda
Sem tabela própria: derivada de `budget_requests` com `status='accepted'`, ordenada por `event_date`. (Evita duplicar estado.)

### 2.5 Auditoria (incluída na primeira fatia)
`budget_audit` nos moldes de `review_audit` (action, actor, reason) para transições de status de pedidos e propostas.

## 3. Rotas propostas

Todas sob `/api`, com Bearer nas rotas de conta (adicionar ao `MOBILE_PRINCIPAL_ROUTES`).

| Método | Rota | Papel | Uso |
|---|---|---|---|
| POST | `/api/photographers/:slug/budget-requests` | client | criar solicitação |
| GET | `/api/account/budget-requests` | client/photographer | listar os próprios (enviados/recebidos) |
| GET | `/api/account/budget-requests/:id` | participante | detalhe + propostas |
| POST | `/api/account/budget-requests/:id/proposals` | photographer | enviar proposta |
| PUT | `/api/account/budget-requests/:id` | participante | ação: `accept`/`reject` (client), `cancel` (client), `withdraw` proposta (photographer) |
| GET | `/api/account/agenda` | photographer | pedidos aceitos por data |
| GET | `/api/account/favorites` | client | listar favoritos |
| POST | `/api/account/favorites` | client | adicionar (por slug/id do fotógrafo) |
| DELETE | `/api/account/favorites/:photographerId` | client | remover |

## 4. Estados e transições

`budget_request`: `pending` → `proposed` (fotógrafo envia proposta) → `accepted`/`rejected` (cliente decide). `pending`/`proposed` → `cancelled` (cliente cancela). Transições inválidas → `409`.

`proposal`: `pending` → `accepted`/`rejected` (cliente) ou `withdrawn` (fotógrafo). Aceitar uma proposta aceita o pedido e rejeita as irmãs.

## 5. Autorização e validação

- Criar pedido: `role=client`; fotógrafo alvo publicado; `client<>photographer`.
- Propor: `role=photographer` e dono do pedido (`photographer_user_id`).
- Aceitar/rejeitar/cancelar: `role=client` e dono do pedido (`client_user_id`).
- Favoritos: `role=client`.
- Validações de campo → `400`; recurso ausente → `404`; não autorizado → `403`; transição/limite → `409`; rate limit → `429`.
- Rate limiting por rota (padrão `ACCOUNT_RATE_LIMITER`/`CONTENT_RATE_LIMITER`).

## 6. Erros

Corpo `{ "error": "texto" }` (padrão atual). Pendência: códigos semânticos estáveis. Android mapeia por status (`httpErrorCodeFor`), sem usar texto como regra.

## 7. Mapeamento ao Android

- `QuoteScreen` → `POST /api/photographers/:slug/budget-requests`.
- `BudgetsScreen` (cliente) → `GET /api/account/budget-requests` (enviados).
- `ProRequests` (fotógrafo) → `GET /api/account/budget-requests` (recebidos) + `POST .../proposals`.
- `ProAgenda` → `GET /api/account/agenda`.
- Aba de favoritos → `GET/POST/DELETE /api/account/favorites`.
- Substituir a entidade Room `Budget` e os dados de demonstração por repositórios remotos; remover métricas fictícias do painel.
- Novos ports de domínio (`BudgetRequestsGateway`, `FavoritesGateway`) + gateways HTTP + parsers + testes JVM, no padrão de perfil/portfólio.

## 8. Testes

- Backend: testes de contrato (sucesso, autorização por papel, transições, limites, 400/401/403/404/409/429) em `test/` + `test-support/api-contracts.mjs`.
- Android: testes JVM de parsers/gateways/ViewModel; instrumentados dos fluxos críticos.
- Atualizar `db/schema.ts` (schemaVersion 15+) em sintonia com as migrações (invariante testada).

## 9. Pendências e aceite

- ~~Aprovação do mantenedor~~ — aprovada (2026-11-14); auditoria incluída; 1 pedido pendente por par.
- Implementação backend (migrações + handlers + testes) e integração Android.
- Validação em staging.

Aceite quando: cliente solicita, fotógrafo propõe, cliente aceita/recusa, agenda reflete aceitos, favoritos persistem — tudo com Bearer, autorização por papel e testes nos dois clientes; web e Android compartilham as mesmas regras.