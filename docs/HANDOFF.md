# Handoff — LensClick App

## Repositórios

- Android: `Renato1909/LensClickApp` — este repositório (branch `main`).
- Plataforma web/API: `Renato1909/LensClick` — fonte de verdade do backend e dos dados de produção.

## Estado técnico

- Aplicativo Android nativo em Kotlin e Jetpack Compose (módulo único `:app`).
- Gradle 9.3.1, AGP 9.1.1, Kotlin 2.2.10, JDK 21.
- `compileSdk`/`targetSdk` 36, `minSdk` 24, `applicationId`: `com.lensclick.app`.
- Navegação declarativa por `Navigation Compose` cobrindo todas as telas.
- Armazenamento de sessão seguro via Android Keystore; Room para cache sem credenciais (migração 2→3 removeu senhas).
- **Integração de Recursos:**
  - **Autenticação & Sessão:** Login, cadastro móvel, restauração via `/me`, logout e revogação via Bearer opaco.
  - **Descoberta & Perfil Público:** Filtro de fotógrafos por cidade/especialidade e leitura de perfil público por `slug`.
  - **Orçamentos, Propostas & Agenda:** Pedidos de orçamento (`/api/photographers/:slug/budget-requests`), envio/aceite de propostas, agenda profissional e favoritos integrados ao backend.
  - **Conversas & Mensagens Remotas:** Integrado às rotas Bearer reais (`/api/account/conversations`, mensagens e envio).
  - **Avaliações Verificadas:** Leitura de avaliações públicas e formulário de avaliação integrados às rotas de produção (`/api/photographers/:slug/reviews`).
  - **LGPD (Exportação e Exclusão):** Exportação de dados (`GET /api/account/export`) e exclusão definitiva de conta (`DELETE /api/account` com confirmação "EXCLUIR" e senha) integradas.
  - **2FA TOTP + Recuperação (ADR 002):** Estrutura completa de domain/gateway/ViewModel/UI no Android (desafio no login `AuthenticationState.TwoFactorRequired`, QR/URI, segredo e códigos de recuperação). Conexão com os endpoints reais pronta; backend em produção responde 403/404 pendente de deploy do Worker.

## Fundação entregue e CI

- **CI Android** em `.github/workflows/android.yml`:
  - Build/lint/testes JVM (Ubuntu, JDK 21, SDK 36.1) com **85 testes JVM passando com 100% de sucesso**.
  - Testes instrumentados em emuladores API 24 e 36 cobrindo fluxo de navegação, Keystore real e autenticação.
  - Correção de seletores e semântica de UI integrados (commit `3ea7890`).

## Instruções para Geração do APK Release Assinado (Feira Tecnológica)

1. Para a demo/feira tecnológica, o APK release deve ser gerado via sideload apontando para a API de produção: `https://lensclick.renato-aparecido-business.workers.dev`.
2. A Keystore e senhas de release **NUNCA** devem ser commitadas no repositório.
3. Para gerar o APK assinado via GUI do Android Studio:
   - Menu: **Build → Generate Signed Bundle / APK...**
   - Selecione **APK** → Escolha/Crie uma Keystore fora da pasta do projeto (ex.: `~/keystores/release.jks`).
   - Preencha alias e senhas, selecione a variante **release** (`:app:release`) e clique em **Create**.
   - O artefato final assinado estará em `app/build/outputs/apk/release/app-release.apk`.
4. Alternativamente via linha de comando (Gradle):
   ```bash
   ./gradlew :app:assembleRelease -Plensclick.releaseKeystorePath=/caminho/fora/do/repo/release.jks -Plensclick.releaseStorePassword=... -Plensclick.releaseKeyAlias=... -Plensclick.releaseKeyPassword=...
   ```

## Próximos passos

1. Acompanhar a liberação/deploy do 2FA no backend (`Renato1909/LensClick`).
2. Validar no dispositivo físico de teste a instalação do APK release via sideload.
3. Reativar domínios e conta Google Play quando a publicação pública for iniciada.

**Atualizado em:** 15 de novembro de 2026.
