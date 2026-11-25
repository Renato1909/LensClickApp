# Status dos Gates — LensClickApp

**Atualização:** 24 de novembro de 2026  
**Base:** `main` em `ae7ce27` (v1.3.0 — contratações e pagamentos, PRs #17/#18)  
**Repositórios:** `Renato1909/LensClickApp` e `Renato1909/LensClick`

| Gate | Estado | Evidência |
|---|---|---|
| A — Fundação Android | Fechado | Gradle Wrapper 9.3.1 versionado, `compileSdk`/`targetSdk` 36 e `minSdk` 24, toolchain JDK 21 resolvido no CI. Últimas execuções na `main` (#45451885295 e #45451885168): build, lint, testes JVM e instrumentados API 24/36 verdes. `local.properties`, keystores e segredos fora do Git. |
| B — Matriz de paridade | Fechado | [`MATRIZ_PARIDADE.md`](./MATRIZ_PARIDADE.md) com a coluna Android preenchida para as capacidades avaliadas; integrada a auth, descoberta, orçamentos, conversas, avaliações, LGPD e 2FA. |
| C — Contrato móvel seguro | Fechado (escopo contratual) | Bearer opaco com `sessionKind`, `Authorization` sem cookie e `AndroidKeystoreSessionStore` (AES-GCM via Android Keystore) em `SessionStore.kt`. Renovação proativa de sessão está **fora do contrato atual** e registrada como diferimento em [`CONTRATO_AUTENTICACAO_MOBILE.md`](./CONTRATO_AUTENTICACAO_MOBILE.md); `EncryptedSharedPreferences` não é exigido porque o token já é cifrado no Keystore. |
| D — Primeira fatia vertical | Fechado | Login, cadastro, descoberta, portfólio público, orçamentos/propostas, agenda, favoritos, avaliações, LGPD e 2FA operacionais; contratações e pagamentos adicionados na v1.3.0 (PR #17/#18). |
| E — Autenticação em duas etapas | Fechado | Alinhado ao backend em produção (PR #160/#161): HTTP 202 tratado antes do `ensureSuccess`, `challengeToken` lido da resposta e validado, e senha digitada pelo usuário na desativação. Cobertura em `TwoFactorGatewayTest`, `MobileAuthRepositoryTest` e `LensClickViewModelTest`. |
| F — Validação e build de release | Concluído | 90 testes JVM sem falhas e testes instrumentados em API 24/36 no CI. O build de release é **assinado no CI** com keystore em Actions secrets; o APK local em `app/build/outputs/` é artefato antigo (1.2) e não deve ser usado como evidência. Geração do AAB e faixas da Play ficam no Gate I. |
| G — Domínio e e-mail | Adiado | Domínio próprio, DNS e remetente institucional dependem de decisão externa (ver `DECISOES.md`). |
| H — Revisão jurídica | Adiado | Parecer formal de Termos/Privacidade e Data safety dependem de profissional externo. |
| I — Publicação na Google Play | Pendente | Requer domínio, jurídico, upload key/Play App Signing, AAB de release e faixa interna → teste fechado → lançamento gradual. |

> Os gates A–F são técnicos e estão fechados com a evidência acima. G, H e I dependem de decisões, recursos externos ou configuração de conta e não podem ser concluídos apenas com código.
