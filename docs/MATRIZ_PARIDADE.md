# Matriz de paridade — web, API e Android

**Auditoria:** 24 de novembro de 2026, fuso America/Sao_Paulo.

**Estado:** Matriz revalidada na versão 1.3.0 (`main` em `ae7ce27`). Inclui o alinhamento do 2FA com o backend (PR #160/#161) e as contratações/pagamentos adicionados nos PRs #17/#18.

## Matriz funcional

| Capacidade | Web e API atuais | Android atual | Estado / Evidência |
|---|---|---|---|
| Cadastro e login básico | Endpoints de auth móvel. W2 | Integrado e testado. | **Integrado** |
| Descoberta e perfil público | GET `/api/photographers` e slug. W4 | Integrado e paginado. | **Integrado** |
| Orçamentos, propostas e agenda | Rotas de orçamentos e propostas. W1/W4 | Integrado com gateways Bearer e telas. | **Integrado** |
| 2FA TOTP (ADR 002) | Política de conta unificada (PR #160/#161). W3/W8 | Integrado: processa HTTP 202, challengeToken real e fluxo de verificação/ativação. | **Integrado** |
| Conversas e mensagens | Inbox e mensagens por HTTP. W3/W4/W7 | Gateway e telas integrados. | **Integrado** |
| Avaliações verificadas | Solicitação, confirmação e review. W4 | Gateway e telas integrados. | **Integrado** |
| LGPD (Exportação e Exclusão) | GET `/api/export`, DELETE `/api/account`. W3 | Gateway e opções na `AccountScreen` integrados. | **Integrado** |
| Contratações e pagamentos | Persistência em `engagements`/`engagement_events` (PR #166). | `EngagementScreens` e `PaymentPanels` integrados na v1.3.0 (PR #17/#18). | **Integrado** |
