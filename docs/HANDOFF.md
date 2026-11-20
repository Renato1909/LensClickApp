# Handoff — LensClick App

## Repositórios

- Android: `Renato1909/LensClickApp` — este repositório (branch `main`).
- Plataforma web/API: `Renato1909/LensClick` — fonte de verdade do backend e dos dados de produção.

## Estado técnico

- Aplicativo Android nativo em Kotlin e Jetpack Compose (módulo único `:app`).
- Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10, JDK 21.
- `compileSdk`/`targetSdk` 36, `minSdk` 24, `applicationId`: `com.lensclick.app`.
- Navegação declarativa por `Navigation Compose`.
- Sessão persistida com segurança no Android Keystore; Room sem credenciais (migração 2→3).
- **Estado atual das integrações (Produção):**
  - **Autenticação básica & Descoberta:** Login, cadastro móvel, restauração via `/me`, logout e descoberta operam contra a API de produção.
  - **Orçamentos, Propostas & Agenda:** Pedidos de orçamento, propostas, agenda e favoritos integrados ao backend (LensClick #153).
  - **2FA TOTP (ADR 002):** Backend em produção (PR #160/#161) ativo. O cliente Android consome o fluxo real: login com 2FA responde HTTP 202 com `challengeToken` real enviado ao endpoint `/api/mobile/auth/2fa/verify`, além de ativação/desativação gerenciadas na conta.
  - **Conversas, Avaliações & LGPD:** Gateways e telas integrados e alinhados aos contratos Bearer da plataforma.

## Fundação entregue e CI

- **CI Android** em `.github/workflows/android.yml`:
  - Build, lint e 85 testes JVM passando com 100% de sucesso.
  - Testes instrumentados cobrindo navegação, Keystore e migração do Room.

## Geração do APK Release Assinado (Sideload para Feira)

1. Compilação da variante `release` limpa (`:app:assembleRelease`).
2. Assinatura local com Keystore fora do repositório (via assistente do Android Studio ou Gradle CLI).

**Atualizado em:** 15 de novembro de 2026.
