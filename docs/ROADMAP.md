# Roadmap do LensClick App

> Evolução do protótipo local em Kotlin para o aplicativo oficial conectado à plataforma LensClick.

## Estado atual

O aplicativo possui 17 telas em Jetpack Compose e persiste dados locais com Room. A autenticação
móvel e a descoberta/perfil público de fotógrafos já consomem a API da plataforma; usuários,
sessões e perfis públicos remotos não são duplicados no Room. Orçamentos, conversas e partes do
modo profissional ainda são protótipos locais e não representam a fonte de verdade de produção.

A camada de dados está separada em `data.local` (Room), `data.remote` (descoberta e `ApiEnvironment`),
`data.remote.auth` (autenticação) e `domain` (tipos puros e portas), com dependência `ui → data → domain`
(commit `9d3d4e6`).

## Fase 1 — fundação Android

- [x] Substituir testes de exemplo por uma linha de base útil (testes JVM de repositório/ViewModel + instrumentado de migração Room 2→3).
- [x] Criar CI para build, lint e testes (`.github/workflows/android.yml`: build debug, testes JVM, lint, instrumentados em emuladores API 24/36).
- [x] Remover credencial do Room (migração 2→3 descarta `passwordHash`; hashes de demonstração vivem apenas em memória no processo).
- [ ] Validar build limpo, lint, testes e instalação em emulador **local** (bloqueado: esta máquina não tem Android SDK; ver `docs/AMBIENTE_ANDROID.md`). A validação ocorre no CI.
- [x] Definir pacote base `com.lensclick.app`, variantes `com.lensclick.app.debug`/`.staging` e separação de ambientes; versionamento permanece pendente.
- [x] Separar melhor domínio, dados locais e dados remotos (`data.local`, `data.remote`, `data.remote.auth`, `domain`; commit `9d3d4e6`).
- [x] Adotar Navigation Compose para as 17 telas, com pilha de retorno e testes de navegação.
- [ ] Definir identificadores de destino e domínio antes de adicionar deep links.
- [x] Impedir persistência de senhas em estado salvo e desabilitar backup/transferência do protótipo local.

## Fase 2 — contrato e paridade

- [x] Criar `docs/MATRIZ_PARIDADE.md` comparando app, web e API (primeira versão, baseada no commit `c3358c2` da plataforma).
- [ ] Aprovar o escopo do primeiro lançamento.
- [x] Definir autenticação móvel revogável com o backend (primeira fatia integrada).
- [x] Definir cliente HTTP, serialização, erros e compatibilidade da API para autenticação e descoberta/perfil público.
- [ ] Definir cache/offline sem duplicar regras de negócio.

## Fase 3 — integração funcional

- [x] Autenticação, sessão e cadastro móvel (verificação de e-mail e conta ainda pendentes).
- [x] Descoberta, perfil público e leitura do portfólio público.
- [ ] Perfil profissional e edição/envio de portfólio.
- [ ] Orçamentos e propostas.
- [ ] Avaliações.
- [ ] Conversas, mensagens e notificações.
- [ ] Exportação e exclusão LGPD.
- [x] Remover credenciais e autenticação locais de demonstração da release; a referência histórica foi retirada do README.

## Fase 4 — segurança e 2FA

- [ ] Produzir threat model e decisão técnica.
- [ ] Armazenar credenciais de sessão com proteção do Android Keystore.
- [ ] Implementar 2FA como política compartilhada com a web.
- [ ] Cobrir recuperação, revogação, rate limiting e auditoria.
- [ ] Revisar logs, screenshots sensíveis e links profundos.

## Fase 5 — validação

- [ ] Testes JVM de regras, ViewModels e integrações.
- [ ] Testes instrumentados dos fluxos críticos.
- [ ] Testes de contrato com a API.
- [ ] Matriz de versões, tamanhos, telefone e tablet.
- [ ] Rede lenta/offline, rotação e retorno do background.
- [ ] TalkBack, foco, contraste, fonte ampliada e alvos de toque.
- [ ] Auditoria de privacidade e segurança da variante release.

## Fase 6 — prontidão pública

- [ ] Domínio próprio e URLs públicas estáveis.
- [ ] E-mail autenticado no Brevo.
- [ ] Revisão jurídica formal dos Termos, Política e mapa de dados.
- [ ] Política de Privacidade pública compatível com o app.
- [ ] Data safety e conteúdo do aplicativo revisados.

## Fase 7 — Google Play

- [ ] Conta de desenvolvedor e confirmação do pacote `com.lensclick.app` no Play Console.
- [ ] Upload key protegida e Play App Signing.
- [ ] AAB de release e materiais PT-BR/EN-US.
- [ ] Faixa interna.
- [ ] Teste fechado e correções do pre-launch report.
- [ ] Lançamento gradual com monitoramento e plano de interrupção.

## Próxima entrega

Pacote/ambientes (`ca1fa77`) e separação de dados locais/remotos/domínio (`9d3d4e6`) concluídos. Próximos passos: revisar o contrato móvel com a plataforma (recuperação de senha, verificação de e-mail e dados da conta), definir cache/offline sem duplicar regras de negócio e preparar perfil profissional/portfólio autenticados sem inventar endpoints. Nenhuma conexão com produção deve começar antes da aprovação e implementação do contrato móvel.
