# Status dos Gates — LensClickApp

**Atualização:** 14 de novembro de 2026  
**Repositórios:** `Renato1909/LensClickApp` e `Renato1909/LensClick`

Este documento separa trabalho integrado, trabalho documentado e dependências externas. Percentuais são estimativas de conclusão do escopo de cada Gate, não uma medição de cobertura de código nem aprovação de publicação.

| Gate | Estado | Estimativa | Evidência principal |
|---|---|---:|---|
| A — Fundação Android | Fechado | 100% | Gradle, JDK 21, SDK 36, CI, lint e testes base integrados; dados separados em `data.local`/`data.remote`/`domain` (`9d3d4e6`). |
| B — Matriz de paridade | Em andamento | 90% | [`MATRIZ_PARIDADE.md`](./MATRIZ_PARIDADE.md) atualizado após autenticação móvel e descoberta/perfil público; faltam decisões de escopo e contratos das próximas fatias. |
| C — Contrato móvel seguro | Fechado para a primeira fatia | 100% | PR backend [#148](https://github.com/Renato1909/LensClick/pull/148), contrato e testes de login, `/me`, logout e revogação. Refresh rotativo e `expiresAt` em `/me` continuam pendências do ADR 001. |
| D — Primeira fatia vertical | Fechado para autenticação e descoberta pública | 100% | Login, cadastro, restauração, logout, descoberta por filtros/paginação, perfil por slug, portfólio público, estados de vazio/erro/404 e testes JVM. |
| E — Autenticação em duas etapas | Não iniciado | 20% | ADR 002 proposto; faltam contrato 200/202, desafio, verificação móvel, migração, auditoria, rollback e aprovação formal. |
| F — Validação integral e publicação | Em andamento | 60% | CI JVM/lint/instrumentados aprovado; cobertura ampliada com JVM de mapeamento/sessão/ViewModel e instrumentados de Keystore e fluxos de auth (`af6e1f4`); faltam staging operacional, dispositivo físico, validação manual de acessibilidade, AAB assinado e smoke de release. |
| G — Domínio, e-mail e jurídico | Não iniciado | 10% | Dependências identificadas no plano; faltam domínio, identidade legal, textos aprovados e Data safety baseada em auditoria final. |
| H — Google Play | Não iniciado | 5% | Ainda faltam conta, confirmação do pacote `com.lensclick.app` no Play Console, Play App Signing, materiais da loja e faixas de teste. |

## O que pode ser feito agora

### Sem decisões externas

- manter a matriz de paridade atualizada;
- ~~concluir testes JVM de mapeamento, sessão e ViewModel~~ — concluído (`af6e1f4`);
- ~~adicionar testes instrumentados de restauração, logout, troca de conta e estados de rede~~ — concluído (`af6e1f4`, Keystore real + fluxos de auth);
- preparar telas e repositórios para perfil profissional e portfólio autenticados sem inventar endpoints;
- revisar textos do README, handoff e validação para não descrever a autenticação ou a descoberta pública como locais;
- manter builds de debug, staging e release separados e sem segredos.

### Dependente de aprovação ou ambiente

- confirmar o pacote `com.lensclick.app` na conta Google Play;
- fornecer endpoint staging operacional;
- aprovar contratos de cadastro, recuperação, verificação de e-mail e conta;
- decidir se orçamentos, propostas, agenda e favoritos entram no primeiro lançamento;
- aprovar o ADR 002 antes de implementar 2FA;
- fornecer dispositivo físico/emulador controlado e validação manual de TalkBack;
- concluir domínio, revisão jurídica e conta Google Play.

## Critério para declarar os Gates concluídos

Um Gate só deve ser fechado quando houver código ou decisão documentada, testes compatíveis com o risco, evidência reproduzível e registro explícito das limitações. Compilação isolada não é evidência suficiente para validação integral ou publicação.
