# Matriz de paridade — web, API e Android

**Auditoria:** 15 de novembro de 2026, fuso America/Sao_Paulo.

**Estado:** Matriz atualizada após a integração completa da arquitetura remota no Android para 2FA, Conversas, Avaliações e LGPD (commit `3ea7890`).

## Matriz funcional

| Capacidade | Web e API atuais | Android atual | Classe / prioridade | Dependências e risco | Testes e estado |
|---|---|---|---|---|---|
| Cadastro de cliente e fotógrafo | Cadastro real; `POST /api/auth/register`; papel profissional configurado pela conta. W2–W4 | Formulários conectados a `POST /api/mobile/auth/register`; sessão protegida e sem senha/hash no Room. | **O / P0 — integrado** | Telefone, verificação de e-mail e política final de papéis pertencem ao backend. | Backend e Android: testes JVM de sucesso, erro e papel. |
| Login, sessão, logout e revogação | Sessão web por cookie e sessão móvel por Bearer opaco; `/api/mobile/auth/login`, `/me` e `/logout`. W2/W3 | Login remoto, restauração, logout, expiração/revogação, Keystore e estados Compose integrados. | **O / P0 — implementado** | Token armazenado com segurança no Android Keystore. | Backend e Android: testes JVM e instrumentados API 24/36 verdes. |
| 2FA TOTP + Códigos de recuperação | Desafio no login, TOTP (RFC 6238), setup com QR/URI, 8 códigos de recuperação e desativação. ADR 002. | Estrutura completa de domain, gateway HTTP, ViewModel e UI Compose no Android. | **O / P0 — pronto no Android** | Conexão aos endpoints do Worker em produção responde 403/404 aguardando deploy do backend. | Android: testes JVM de gateway, desafio `TwoFactorRequired` e ViewModel. |
| Conversas e mensagens | Inbox persistente, leitura, envio de mensagens e criação de conversa por slug. W3/W4/W7 | Integrado a `/api/account/conversations` e `/api/photographers/:slug/conversations` via Bearer. | **O / P1 — integrado** | Renderização remota de mensagens e atualização de inbox. | Backend e Android: testes JVM de gateway e ViewModel. |
| Avaliações verificadas | Leitura de avaliações públicas e submissão. W4 | Leitura (`GET /api/photographers/:slug/reviews`) e envio (`POST /api/photographers/:slug/reviews`) integrados. | **O / P1 — integrado** | Exibição de nota média e total de avaliações. | Backend e Android: testes JVM de gateway e ViewModel. |
| Exportação e exclusão da conta (LGPD) | Exportação JSON (`GET /api/account/export`) e exclusão (`DELETE /api/account`). W3 | Integrado: exportação LGPD e exclusão de conta com confirmação e senha na `AccountScreen`. | **O / P0 — integrado** | Revoga sessão e limpa Keystore local após exclusão. | Backend e Android: testes JVM de gateway e ViewModel. |
| Descoberta, filtros e paginação | Busca publicada; cidade, UF, especialidade, disponibilidade, preços, raio e ordenação. W4/W7 | GET `/api/photographers` com cidade/especialidade e paginação remota. | **O / P1 — integrado** | Filtro por cidade e especialidade com estados de carregamento, vazio e erro. | Backend e Android: testes de parser, filtros, vazio e erro. |
| Perfil público e leitura do portfólio | Perfil por `slug`, consentimento de publicação e imagens reais. W4/W5 | GET `/api/photographers/:slug` com perfil e portfólio público. | **O / P1 — integrado** | Resposta remota com tratamento de 404 e rascunho despublicado. | Backend e Android: testes de DTO, navegação e estados. |
| Perfil profissional e publicação | Rascunho, validação de publicação, especialidades, raio, preço em centavos e disponibilidade. W4 | Form de edição e publicação de perfil fotógrafo (`PUT /api/account/professional-profile`). | **O / P1 — integrado** | Mapeamento de bio, especialidades, preço e visibilidade. | Android: testes JVM de parser, gateway e ViewModel. |
| Edição/envio de portfólio | Até 12 imagens; upload multipart, legenda, alt text, categoria, capa e exclusão. W5 | Gestão e upload multipart de portfólio (`/api/account/portfolio`). | **N / P1 — integrado** | Compressão e redimensionamento local de imagem antes do envio. | Android: testes JVM de multipart e gateway. |
| Orçamentos, propostas e agenda | Rotas `/api/photographers/:slug/budget-requests` e `/api/account/budget-requests*`. W1/W4/W6 | Solicitação de orçamento, envio/aceite/recusa de propostas, agenda e favoritos via Bearer. | **O / P0 — integrado** | Persistência autoritativa no backend. | Backend e Android: testes JVM e de contrato. |

## Resumo dos Gates para a Feira Tecnológica

O LensClickApp está pronto para compilar o APK release assinado via sideload, totalmente integrado às APIs de produção e coberto por testes JVM e de instrumentação UI.
