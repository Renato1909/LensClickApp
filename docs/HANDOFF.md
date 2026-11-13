# Handoff — LensClick App

## Repositórios

- Android: `Renato1909/LensClickApp` — este repositório.
- Plataforma web/API: `Renato1909/LensClick` — fonte de verdade do backend e dos dados de produção.

## Estado técnico

- aplicativo Android nativo em Kotlin e Jetpack Compose;
- Android Studio, módulo único `:app`;
- Kotlin 2.2.10, AGP 9.1.1 e Gradle 9.3.1;
- `compileSdk`/`targetSdk` 36 e `minSdk` 24;
- `applicationId` atual: `com.lensclick.app`;
- 17 telas navegáveis em uma única Activity;
- navegação por `Navigation Compose` nas 17 telas; identificadores/deep links externos ainda pendentes;
- dados locais em Room para usuários, fotógrafos e orçamentos (schema v3, exportado em `app/schemas/com.lensclick.app.data.local.LensClickDatabase/`);
- camada de dados separada em `data.local` (Room), `data.remote` (descoberta/`ApiEnvironment`), `data.remote.auth` (autenticação) e `domain` (tipos puros/portas), com dependência `ui → data → domain` (commit `9d3d4e6`);
- **Room não persiste mais senhas**: a migração 2→3 removeu a coluna `passwordHash`; os hashes SHA-256 de demonstração vivem apenas em memória (`ConcurrentHashMap` no repositório) e desaparecem ao reiniciar o processo;
- backend real conectado na primeira fatia de autenticação móvel: login, restauração via `/me`, logout, expiração e revogação;
- conta de demonstração não é mais usada pelo fluxo de autenticação conectado e não pode voltar à release pública;
- cadastro móvel já usa o contrato remoto; orçamentos, mensagens e perfil profissional ainda não possuem contratos móveis implementados.

## Fundação entregue (integrada em `main` via PR #4, commit `fbbd934`)

- **CI Android** em `.github/workflows/android.yml`: job de build/lint/testes JVM (Ubuntu, JDK 21, SDK 36.1) e jobs instrumentados em emuladores API 24 e 36, actions fixadas por SHA, sem segredos.
- **Testes úteis**: `LensClickRepositoryTest` (7 casos), `LensClickViewModelTest` (5 casos) com `FakeLensClickDao` em memória; `LensClickMigrationTest` instrumentado valida que a migração 2→3 descarta `passwordHash` e preserva usuários.
- **Documentos novos**: `docs/AMBIENTE_ANDROID.md` (diagnóstico do ambiente e requisitos exatos) e `docs/MATRIZ_PARIDADE.md` (paridade app × web/API no commit `c3358c2` da plataforma, com classificação O/N/W/A).
- **Preparação arquitetural mínima**: `LensClickViewModel` recebe o repositório por injeção via `Factory` (testável em JVM), e o Room exporta schema para versionamento.
- **Primeira fatia integrada**: cliente HTTP móvel, DTOs, mapeamento de erros, armazenamento com Android Keystore, estados de autenticação e Navigation Compose foram mergeados no PR #8. A fatia seguinte integrou descoberta e perfil público por slug, portfólio e estados de publicação.
- **Segurança local e navegação (entrega seguinte)**: senhas de formulário usam `remember`, sem estado restaurável; backup e transferência do protótipo são excluídos; NavHost substitui o estado de tela anterior, com testes instrumentados de retorno e logout. Confirmar o PR e checks desta entrega antes de considerá-la integrada.

## Limitação do ambiente local

Esta máquina **não possui Android SDK, ADB ou emulador** (`ANDROID_HOME`/`sdk.dir` ausentes); o build local falha em `SDK location not found` antes de compilar. Nada foi instalado sem autorização. Diagnóstico completo, requisitos exatos (`platforms;android-36.1`, Build Tools 36.0.0, JDK 21 como daemon) e comandos em `docs/AMBIENTE_ANDROID.md`. A validação de build/testes/lint acontece no CI do GitHub.

## Direção aprovada

O aplicativo deve consumir a API da plataforma LensClick e compartilhar usuários, autorização, dados e 2FA com a web. Não criar backend, base de usuários ou regra de negócio paralelos no Android.

## Próximos passos

1. ~~Validar build, lint, testes~~ — validado via CI; validação **local** segue bloqueada pela ausência de SDK (ver `docs/AMBIENTE_ANDROID.md`).
2. ~~Criar CI Android~~ — concluído (`.github/workflows/android.yml`); o mantenedor deve marcar os checks como obrigatórios na proteção da `main`.
3. ~~Produzir `docs/MATRIZ_PARIDADE.md`~~ — primeira versão entregue; atualizar a cada fatia.
4. **Atualizar esta matriz de paridade** a cada nova fatia e registrar contratos, testes e limitações.
5. Confirmar com o mantenedor o pacote `com.lensclick.app` na conta Google Play, além do endpoint staging, política de papéis e destino da UX de orçamentos/propostas que não existe na API.
6. Definir IDs reais, domínio e políticas de link profundo; integrar descoberta e perfis públicos somente após contrato.
7. Fechar os contratos de recuperação de senha, verificação de e-mail e dados da conta.
8. Aprovar o threat model e o ADR 002 antes de implementar 2FA.
9. Concluir validação integral em staging/dispositivo real, revisão jurídica e publicação por faixas.

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

- confirmação do pacote `com.lensclick.app` e da conta Google Play;
- contrato móvel implementado no backend `LensClick`;
- domínio próprio e e-mail autenticado;
- revisão jurídica formal;
- aparelhos reais para validação final.

**Atualizado em:** 13 de novembro de 2026.
