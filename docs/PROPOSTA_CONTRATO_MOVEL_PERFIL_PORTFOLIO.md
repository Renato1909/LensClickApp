# Proposta de contrato móvel — perfil profissional e portfólio

Status: **APROVADO pelo mantenedor.** Camada de dados remota implementada em `a88f1ca` (gateways `HttpProfessionalProfileGateway`/`HttpPortfolioGateway`, parsers DTO, upload multipart, mapeamento de erros e testes JVM), consumindo as rotas de conta existentes com `Authorization: Bearer`. Telas de perfil profissional e portfólio implementadas (`ff36af3`/`2bd4d31`); validação em staging e testes de contrato pendentes.
Data: 2026-11-14

- Backend auditado: `Renato1909/LensClick` @ `6659948` (`src/router.mjs`, `src/photographers.mjs`, `src/portfolio.mjs`, `src/account.mjs`, `src/worker.mjs`).
- Android: `Renato1909/LensClickApp` @ `a88f1ca` — domínio preparado (`434af51`) e gateways remotos implementados (`a88f1ca`): `HttpProfessionalProfileGateway`/`HttpPortfolioGateway` + parsers + testes JVM.
- Contrato base: [`CONTRATO_AUTENTICACAO_MOBILE.md`](./CONTRATO_AUTENTICACAO_MOBILE.md).

## 1. Objetivo

Permitir que o fotógrafo autenticado gerencie pelo app o seu perfil profissional (rascunho e publicação) e o seu portfólio (upload, edição, ordem, capa e exclusão), consumindo o mesmo backend da web, sem regra de negócio paralela e sem criar endpoints novos.

## 2. Contexto e regra principal

- O roteador já inclui estas rotas em `MOBILE_PRINCIPAL_ROUTES` e faz bypass da checagem de Origin/cookie quando a requisição tem `Authorization`. Portanto o app consome as rotas `/api/account/*` **existentes** com Bearer — **não** são necessários novos endpoints `/api/mobile/account/*`.
- O backend permanece a autoridade de autorização, validação, limites e critérios de publicação. O Android não duplica essas regras nem persiste credenciais no Room.
- Sessão móvel revogável, idêntica à do contrato de autenticação: token opaco no Android Keystore, `401` definitivo limpa a sessão local.

## 3. Autenticação e autorização

- Header `Authorization: Bearer <accessToken>` em todas as rotas abaixo.
- `401`: sessão ausente, expirada ou revogada → limpar sessão local.
- `403`: papel insuficiente. `PUT /api/account/professional-profile` e `POST /api/account/portfolio` exigem `role = photographer`. `GET /api/account/professional-profile` e `GET /api/account/portfolio` aceitam qualquer usuário autenticado (retornam rascunho padrão / lista vazia quando não há dados).
- Não reutilizar cookie web como autenticação nativa; não forjar `Origin`.

## 4. Perfil profissional

### 4.1 Ler — `GET /api/account/professional-profile`

Resposta `200` `{ "profile": { ... } }`. Sem perfil salvo, retorna rascunho padrão (`slug` derivado de nome+id, campos vazios, `isPublished = false`).

Shape do `profile` na visão do dono (`ownerView`):

```json
{
  "slug": "nome-123",
  "bio": "",
  "specialties": [],
  "city": "",
  "state": "",
  "serviceRadiusKm": null,
  "startingPriceCents": null,
  "availability": "available",
  "ratingAverage": null,
  "reviewCount": 0,
  "instagram": "",
  "website": "",
  "publicEmail": "",
  "publicPhone": "",
  "showEmail": false,
  "showPhone": false,
  "isPublished": false
}
```

### 4.2 Salvar / publicar — `PUT /api/account/professional-profile`

Rate limit: 30/hora. Exige `role = photographer` (`403`).

Request (campos editáveis):

| Campo | Tipo / limite |
|---|---|
| `bio` | texto ≤ 1200 |
| `specialties` | array de strings |
| `city` | texto ≤ 80 |
| `state` | UF, 2 letras maiúsculas (`/^[A-Z]{2}$/`) |
| `availability` | `available` \| `limited` \| `unavailable` (padrão `available`) |
| `serviceRadiusKm` | `null` ou inteiro 0..1000 |
| `startingPriceCents` | `null` ou inteiro 0..100000000 (centavos) |
| `publicEmail` | e-mail válido (opcional) |
| `publicPhone` | texto ≥ 8 e ≤ 30 (opcional) |
| `instagram` | ≤ 30, `[A-Za-z0-9._]`, sem `@` inicial (opcional) |
| `website` | URL válida ≤ 300 (opcional) |
| `showEmail` | bool; só efetivo com `publicEmail` |
| `showPhone` | bool; só efetivo com `publicPhone` |
| `isPublished` | bool |

Resposta `200` `{ "profile": { ... }, "message": "..." }`.

- Validação de rascunho → `400` `"Revise os dados profissionais informados."` quando: `state` fora do padrão, `serviceRadiusKm`/`startingPriceCents` fora dos limites, `publicEmail` inválido, `publicPhone` < 8, `instagram` inválido ou `website` inválido.
- **Critérios de publicação (servidor):** `isPublished = true` exige `bio` ≥ 60 caracteres, ≥ 1 especialidade, `city` ≥ 2, `state` UF válida, foto de perfil existente (`profile_pic_key`) e ao menos um contato público (`showEmail || showPhone || instagram || website`). Não atendido → `400` `"Para publicar, adicione foto, apresentacao, especialidade, localizacao e ao menos um contato profissional."`.
- O `slug` é estável: derivado de nome+id na criação e preservado nas atualizações.
- A foto de perfil é gerida por `PUT /api/profile` e `DELETE /api/account/photo` (fora deste contrato, mas pré-requisito de publicação).

## 5. Portfólio

Constantes do servidor: máximo **12 itens**; imagem **JPG/PNG/WebP ≤ 4.000.000 bytes**; categorias `wedding, events, portrait, family, corporate, product, fashion, newborn, nature, real-estate, other`; no máximo **1 capa**.

### 5.1 Listar — `GET /api/account/portfolio`

`200` `{ "portfolio": [ item ] }`, ordenado por `position, id`. Qualquer usuário autenticado (vazio se nenhum).

```json
{
  "id": 7,
  "caption": "",
  "altText": "",
  "position": 0,
  "isCover": false,
  "category": "other",
  "imageUrl": "/api/account/portfolio/7/image?v=<updated_at>"
}
```

### 5.2 Enviar — `POST /api/account/portfolio`

Rate limit: 20/hora. Exige `role = photographer` (`403`). `multipart/form-data`:

| Campo | Regra |
|---|---|
| `image` | File JPG/PNG/WebP, 1..4.000.000 bytes |
| `altText` | texto ≥ 3 e ≤ 160 (acessibilidade) |
| `caption` | texto ≤ 240 |
| `category` | enum acima (padrão `other`) |

- `409` se já houver 12 itens; `415` se não for multipart; `413` se exceder 4 MB; `400` para categoria/tipo/`altText` inválidos.
- `201` `{ "item": { ... }, "message": "..." }`. Armazenamento privado em R2 (`portfolio/{userId}/{uuid}.{ext}`).

### 5.3 Reordenar / editar — `PUT /api/account/portfolio`

Request `{ "items": [ { "id", "caption", "altText", "position", "isCover", "category" } ] }` (≤ 12).

- `400`: array inválido, > 12 itens, > 1 capa, ids não inteiros/duplicados, `altText` < 3, categoria inválida.
- `403`: item que não pertence ao usuário.
- A posição é o índice no array; `is_cover` é resetado e reaplicado (no máximo 1). `200` `{ "portfolio": [ ... ], "message": "..." }`.

### 5.4 Imagem — `GET /api/account/portfolio/:id/image`

Bearer; item do dono (`404` se ausente). Cache privado (`private, max-age=300, must-revalidate`). Retorna a imagem.

### 5.5 Excluir — `DELETE /api/account/portfolio/:id`

Bearer; item do dono (`404`). Remove do R2 e do banco. `200` `{ "message": "..." }`.

## 6. Erros HTTP

| HTTP | Significado | Comportamento Android |
|---|---|---|
| 200 | sucesso | atualizar estado |
| 201 | criado (upload) | atualizar estado |
| 400 | entrada inválida / publicação não atendida | erro de formulário contextual |
| 401 | não autenticado | limpar sessão |
| 403 | papel insuficiente / item não autorizado | acesso negado |
| 404 | recurso ausente | recurso ausente |
| 409 | conflito (limite de 12 itens) | erro contextual |
| 413 | imagem > 4 MB | erro de tamanho |
| 415 | não multipart | erro de formato |
| 429 | rate limit | aguardar, sem retry agressivo |
| 5xx | falha do servidor | erro transitório / retry controlado |

Formato atual do corpo: `{ "error": "texto" }`. **Pendência:** códigos de erro semânticos estáveis. O Android mapeia por status de forma determinística (não usa texto como regra de negócio); o mapeamento compartilhado (`httpErrorCodeFor`) cobre 400/401/403/404/409/429/5xx e 413/415 (`INVALID_INPUT`), implementado em `a88f1ca`.

## 7. Mapeamento ao domínio Android preparado

| Port (domain) | Rota |
|---|---|
| `ProfessionalProfileGateway.load()` | `GET /api/account/professional-profile` |
| `ProfessionalProfileGateway.save(profile)` | `PUT /api/account/professional-profile` |
| `PortfolioGateway.list()` | `GET /api/account/portfolio` |
| `PortfolioGateway.add(image, imageContentType, altText, caption, category)` | `POST /api/account/portfolio` (multipart) |
| `PortfolioGateway.update(items)` | `PUT /api/account/portfolio` |
| `PortfolioGateway.remove(id)` | `DELETE /api/account/portfolio/:id` |

Modelo `ProfessionalProfile` alinhado ao `ownerView` (`slug`, `instagram`, `website`, `ratingAverage`, `reviewCount`, `publicEmail`/`publicPhone`) e `PortfolioGateway.add` refinado com o tipo MIME da imagem. Implementação remota concluída em `a88f1ca` (`HttpProfessionalProfileGateway`/`HttpPortfolioGateway`, parsers DTO, upload multipart, transporte binário, mapeamento de erros compartilhado), com testes JVM. As telas vêm na próxima fatia.

## 8. Segurança e privacidade

- HTTPS fora de ambientes locais; token nunca em URL, logs, analytics, screenshots ou mensagens de erro.
- Imagens de portfólio são privadas (cache `private`); não expor entre contas; `imageUrl` do dono requer Bearer.
- Não persistir credenciais no Room; cache remoto particionado por sessão/conta e limpo no logout.
- Backend autoridade de autorização, validação, limites e publicação; rate limiting no backend.
- Seletor de mídia Android com escopo mínimo; não pedir permissão ampla sem necessidade.

## 9. Pendências e critérios de aceite

Pendências:

- ~~aprovação formal do mantenedor~~ — aprovada;
- ~~alinhamento do modelo de domínio ao `ownerView`~~ — concluído (`a88f1ca`);
- ~~camada de dados remota + testes JVM~~ — concluído (`a88f1ca`);
- códigos de erro semânticos estáveis no corpo (backend);
- testes de contrato móveis em ambiente controlado (ownership, capa única, limite de 12, critérios de publicação);
- ~~telas de perfil profissional e portfólio~~ — implementadas (`ff36af3`/`2bd4d31`);
- decisão de escopo (fotógrafo no primeiro lançamento);
- staging operacional para validação.

O contrato estará aprovado quando:

- o app gerenciar perfil (rascunho/publicação) e portfólio (upload/ordem/capa/exclusão) com Bearer, sem cookie;
- a publicação respeitar os critérios do servidor (`400` determinístico);
- limites e ownership forem aplicados pelo servidor e refletidos no app;
- testes cobrirem os fluxos e erros;
- web e Android compartilharem o mesmo usuário e as mesmas regras de negócio.

## 10. Fontes

- Backend `6659948`: `router.mjs` (`MOBILE_PRINCIPAL_ROUTES`, bypass de Origin), `photographers.mjs` (`professionalProfileRead`/`professionalProfileUpdate`), `portfolio.mjs` (`create`/`update`/`image`/`remove` + constantes), `account.mjs` (`portfolioList`), `worker.mjs` (mappers `professionalProfile`/`portfolioItem`).
- Android `434af51`: `domain/ProfessionalProfile.kt`, `domain/Portfolio.kt`, testes JVM de modelo/port.
- [`CONTRATO_AUTENTICACAO_MOBILE.md`](./CONTRATO_AUTENTICACAO_MOBILE.md) e [`MATRIZ_PARIDADE.md`](./MATRIZ_PARIDADE.md).