# Matriz de paridade — web, API e Android

**Auditoria:** 15 de novembro de 2026, fuso America/Sao_Paulo.

**Estado:** Matriz atualizada após o deploy do 2FA no backend (PR #160/#161) e alinhamento do cliente Android para processar HTTP 202 e tokens de desafio reais.

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
