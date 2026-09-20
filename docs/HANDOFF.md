# Handoff — LensClick App

## Repositórios

- Android: `Renato1909/LensClickApp` — este repositório.
- Plataforma web/API: `Renato1909/LensClick` — fonte de verdade do backend e dos dados de produção.

## Estado técnico

- aplicativo Android nativo em Kotlin e Jetpack Compose;
- Android Studio, módulo único `:app`;
- Kotlin 2.2.10, AGP 9.1.1 e Gradle 9.3.1;
- `compileSdk`/`targetSdk` 36 e `minSdk` 24;
- `applicationId` atual: `com.example.lensclickapp`;
- 17 telas navegáveis em uma única Activity;
- navegação atual por estado (`enum class Screen` + `rememberSaveable`);
- dados locais em Room para usuários, fotógrafos e orçamentos;
- backend real ainda não conectado;
- conta de demonstração e hash SHA-256 local são somente protótipo e não podem chegar à release pública como autenticação real.

## Direção aprovada

O aplicativo deve consumir a API da plataforma LensClick e compartilhar usuários, autorização, dados e 2FA com a web. Não criar backend, base de usuários ou regra de negócio paralelos no Android.

## Próximos passos

1. Validar build, lint, testes e execução em emulador.
2. Criar CI Android.
3. Produzir `docs/MATRIZ_PARIDADE.md` com base nas telas existentes e nos contratos atuais da API.
4. Definir com o repositório da plataforma o contrato móvel de autenticação revogável.
5. Integrar por fatias, começando por autenticação e conta.
6. Projetar 2FA para web e Android após threat model.
7. Concluir domínio, revisão jurídica, validação integral e publicação por faixas.

## Restrições

- não reutilizar o cookie de navegador como atalho para autenticação nativa;
- não persistir senha real no Room;
- não versionar keystore, tokens, segredos ou arquivos locais do Android Studio;
- não apontar builds de desenvolvimento para produção sem controle explícito;
- não preencher Data safety ou declarações jurídicas por suposição;
- não publicar diretamente em produção na Google Play.

## Documentos operacionais

- `docs/ROADMAP.md` — sequência de evolução do aplicativo.
- `docs/PLANO_CONVERGENCIA_VALIDACAO_PUBLICACAO.md` — gates completos de integração, 2FA, validação, domínio, jurídico e Google Play.
- `README.md` — visão atual do aplicativo e instruções de execução.

## Dependências externas reais

- decisão do pacote definitivo e da conta Google Play;
- contrato móvel implementado no backend `LensClick`;
- domínio próprio e e-mail autenticado;
- revisão jurídica formal;
- aparelhos reais para validação final.

**Atualizado em:** 19 de setembro de 2026.

