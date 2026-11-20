# Handoff — LensClick App

## Repositórios

- Android: `Renato1909/LensClickApp` — este repositório (branch `main`).
- Plataforma web/API: `Renato1909/LensClick` — fonte de verdade do backend e dos dados de produção.

## Estado técnico

- Aplicativo Android nativo em Kotlin e Jetpack Compose (módulo único `:app`).
- Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10, JDK 21.
- `compileSdk`/`targetSdk` 36, `minSdk` 24, `applicationId`: `com.lensclick.app`.
- Navegação declarativa por `Navigation Compose`.
- Sessão persistida de forma segura com Android Keystore; Room sem credenciais (migração 2→3 removeu senhas).
- **Estado atual das integrações:**
  - **Autenticação básica & Descoberta:** Login, cadastro móvel, restauração via `/me`, logout e descoberta por filtros/paginação operam contra o backend de produção.
  - **Orçamentos, Propostas & Agenda:** Pedidos de orçamento, propostas, agenda e favoritos integrados ao backend (LensClick #153).
  - **2FA TOTP (ADR 002):** Camada de domínio, gateway, desafio no login (`TwoFactorRequired`) e telas implementadas no app; testes de sondagem indicam que os endpoints em `/api/account/2fa/*` retornam 403/404 em produção, aguardando deploy do contrato no Worker backend.
  - **Conversas, Avaliações & LGPD:** Gateways e telas implementados para conversas, avaliações e exportação/exclusão de conta (LGPD), porém pendentes de validação end-to-end em ambiente operacional e staging/dispositivo real.

## Fundação entregue e CI

- **CI Android** em `.github/workflows/android.yml`:
  - Build, lint e 85 testes JVM passando com 100% de sucesso.
  - Testes instrumentados cobrindo navegação (com rolagem em telas pequenas), Keystore e migração do Room.

## Geração do APK Release Assinado (Sideload para Feira)

1. O projeto compila a variante `release` limpa (`:app:assembleRelease`), gerando `app-release-unsigned.apk`.
2. Para assinar o APK para sideload (sem versionar chaves ou senhas no Git):
   - Via assistente do Android Studio: **Build → Generate Signed Bundle / APK...** (criando ou selecionando a Keystore fora da pasta do projeto).
   - Via Gradle CLI: fornecendo as propriedades `lensclick.releaseKeystorePath`, `lensclick.releaseStorePassword`, `lensclick.releaseKeyAlias` e `lensclick.releaseKeyPassword`.

**Atualizado em:** 15 de novembro de 2026.
