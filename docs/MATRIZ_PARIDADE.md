# Matriz de paridade — web, API e Android

**Auditoria:** 15 de novembro de 2026, fuso America/Sao_Paulo.

**Estado:** Matriz reflete o estado atual: autenticação básica, descoberta e orçamentos validados com contratos; 2FA, conversas, avaliações e LGPD implementados na camada cliente com dependência de contrato/deploy backend em produção.

## Matriz funcional resumida

| Capacidade | Web e API atuais | Android atual | Estado / Evidência |
|---|---|---|---|
| Cadastro e login básico | Endpoints de auth móvel. W2 | Integrado e testado (JVM + instrumentado). | **Integrado** |
| Descoberta e perfil público | GET `/api/photographers` e slug. W4 | Integrado e paginado. | **Integrado** |
| Orçamentos, propostas e agenda | Rotas de orçamentos e propostas (LensClick #153). W1/W4 | Integrado com gateways Bearer e telas. | **Integrado** |
| 2FA TOTP (ADR 002) | Política de conta unificada. W3/W8 | Domínio, ViewModel e UI implementados; endpoints em produção retornam 403/404 (aguardando backend). | **Pendente de Backend** |
| Conversas e mensagens | Inbox e mensagens por HTTP. W3/W4 | Gateway e telas implementados; validação e2e pendente em produção. | **Implementação Cliente** |
| Avaliações verificadas | Solicitação, confirmação e review. W4 | Gateway e telas implementados (leitura e envio); pendente confirmação de contrato rigoroso (`review-requests`). | **Implementação Cliente** |
| LGPD (Exportação e Exclusão) | GET `/api/export`, DELETE `/api/account`. W3 | Gateway e opções na `AccountScreen` implementados; validação e2e pendente. | **Implementação Cliente** |
