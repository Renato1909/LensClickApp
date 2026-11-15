# Decisões do mantenedor — LensClick App

Data: 2026-11-14
Registradas por: Renato (mantenedor), com apoio do agente.

## 1. Contexto

O aplicativo será apresentado na **Feira Tecnológica** como demonstração, distribuído por **APK instalado diretamente no aparelho (sideload)**. Nesta fase:

- **não** haverá publicação na Google Play;
- **não** se aplica conta de desenvolvedor, Play App Signing ou faixas de teste;
- **não** haverá domínio próprio nem revisão jurídica de publicação pública;
- o backend usado é o de **produção** `https://lensclick.renato-aparecido-business.workers.dev`.

O "primeiro lançamento" referido nos demais documentos corresponde, nesta fase, à **demo da feira**. Publicação pública futura reativa os gates de domínio, jurídico e Google Play.

## 2. Decisões

| # | Tema | Decisão | Impacto |
|---|---|---|---|
| 1 | Distribuição | APK sideload para a feira; Google Play adiada | Sem Play Console/conta; APK release assinado com chave local (fora do Git) |
| 2 | Papéis | Cliente **e** fotógrafo na demo | Perfil profissional e portfólio (já implementados) entram |
| 3 | Orçamentos/propostas/agenda/favoritos | **Implementar contrato no backend** LensClick antes da feira | Escopo completo; novo trabalho de backend + integração Android |
| 4 | Administração | **Web-only** | App reflete `403` e conteúdo moderado; sem ferramentas admin no celular |
| 5 | 2FA | **TOTP + códigos de recuperação** (compartilhado web/Android) | Ver [`ADR_002_2FA.md`](./ADR_002_2FA.md); implementação backend + Android pendente |
| 6 | Domínio | **Adiar**; manter `workers.dev` | Identidade pública provisória; sem DKIM/DMARC de domínio próprio |
| 7 | Jurídico | **Adiar** | Demo da feira não é publicação pública; revisão formal antes de qualquer publicação futura |
| 8 | Backend do APK | **Produção** `workers.dev` | Contas reais; APK release assinado com chave local |

## 3. Encaminhamentos

1. **Contrato de orçamentos/propostas** no backend `LensClick` (rotas, tabelas, autorização) e integração Android — antes da feira.
2. **2FA TOTP**: implementar backend (enrollment/verify/recovery) + Android, conforme ADR 002 e threat model.
3. **APK da feira**: gerar AAB/APK release assinado com chave local não versionada, apontando para produção; instalar por sideload.
4. **Gates G (domínio/jurídico) e H (Google Play)**: adiados para a demo; reativados se houver publicação pública.

## 4. Riscos e cuidados

- **Orçamentos ainda locais** até o contrato backend existir: não apresentar contratação real na demo enquanto for protótipo local.
- **2FA ainda não implementado**: enquanto isso, contas protegidas apenas por senha; risco de tomada de conta aceito temporariamente para a demo, com 2FA planejado.
- **Backend de produção na demo**: usar contas de demonstração criadas para a feira; não expor dados reais de terceiros; tokens nunca em logs/screenshots.
- **workers.dev sem domínio próprio**: identidade pública provisória, adequada à feira.
- **APK sideload**: a chave de assinatura local deve ser guardada com segurança e nunca versionada; sem Play App Signing nesta fase.

## 5. Referências

- [`ADR_002_2FA.md`](./ADR_002_2FA.md) — decisão e threat model de 2FA.
- [`PLANO_CONVERGENCIA_VALIDACAO_PUBLICACAO.md`](./PLANO_CONVERGENCIA_VALIDACAO_PUBLICACAO.md) — gates (G/H adiados para a demo).
- [`MATRIZ_PARIDADE.md`](./MATRIZ_PARIDADE.md) — paridade e classificação de escopo.
- [`STATUS_GATES.md`](./STATUS_GATES.md) — estado dos gates.