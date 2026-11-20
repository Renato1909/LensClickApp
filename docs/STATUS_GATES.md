# Status dos Gates — LensClickApp

**Atualização:** 15 de novembro de 2026  
**Repositórios:** `Renato1909/LensClickApp` e `Renato1909/LensClick`

| Gate | Estado | Estimativa | Evidência principal |
|---|---|---:|---|
| A — Fundação Android | Fechado | 100% | Gradle 9.3.1, JDK 21, SDK 36, CI, lint e testes base integrados; dados separados em `data.local`/`data.remote`/`domain`. |
| B — Matriz de paridade | Concluído para Demo | 95% | [`MATRIZ_PARIDADE.md`](./MATRIZ_PARIDADE.md) atualizado com auth, descoberta, orçamentos, conversas, reviews, LGPD e 2FA. |
| C — Contrato móvel seguro | Fechado | 100% | Contrato de login, `/me`, logout, revogação, orçamentos, conversas, avaliações e LGPD integrados via Bearer opaco no Keystore. |
| D — Primeira fatia vertical | Fechado | 100% | Login, cadastro, restauração, logout, descoberta, perfil por slug, portfólio, orçamentos, propostas, agenda e favoritos. |
| E — Autenticação em duas etapas | Pronto no Android | 85% | ADR 002 implementado no Android (modelos, gateway, desafio no login `TwoFactorRequired`, QR/URI, códigos de recuperação e desativação); pendente apenas deploy do Worker backend. |
| F — Validação integral e publicação | Em andamento | 80% | CI JVM/lint/instrumentados aprovados (85 testes JVM + instrumentados API 24/36 verdes); APK release compilando limpo para assinatura local. |
| G — Domínio, e-mail e jurídico | Adiado (demo da feira) | 10% | Adiado para a demo via APK sideload (ver `DECISOES.md`). |
| H — Google Play | Não se aplica à demo | 0% | Demo da feira via APK sideload, sem Play (ver `DECISOES.md`). |

## Status para a Feira Tecnológica

O app está 100% pronto para a demo via APK release assinado por sideload, consumindo a API de produção do Cloudflare Worker.
