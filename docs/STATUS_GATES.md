# Status dos Gates — LensClickApp

**Atualização:** 15 de novembro de 2026  
**Repositórios:** `Renato1909/LensClickApp` e `Renato1909/LensClick`

| Gate | Estado | Evidência |
|---|---|---|
| A — Fundação Android | Fechado (100%) | Gradle, JDK 21, SDK 36, CI verde em testes JVM e instrumentados. |
| B — Matriz de paridade | Atualizada | [`MATRIZ_PARIDADE.md`](./MATRIZ_PARIDADE.md) com status real de cada capacidade. |
| C — Contrato móvel seguro | Fechado para fatia core | Autenticação, descoberta e orçamentos operacionais; demais fluxos com cliente pronto e backend pendente/em validação. |
| D — Primeira fatia vertical | Fechado | Login, descoberta, portfólio público, orçamentos, propostas e agenda funcionais. |
| E — Autenticação em duas etapas | Em progresso (Parcial) | Cliente Android estruturado conforme ADR 002; aguardando disponibilidade das rotas no Worker backend. |
| F — Validação e APK release | Em andamento | Testes JVM verdes; APK release unsigned gerado com sucesso; assinatura local via Keystore fora do Git. |
| G / H — Domínio, jurídico e Google Play | Adiado | Adiado para a demo via APK sideload (ver `DECISOES.md`). |
