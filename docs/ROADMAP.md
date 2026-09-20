# Roadmap do LensClick App

> Evolução do protótipo local em Kotlin para o aplicativo oficial conectado à plataforma LensClick.

## Estado atual

O aplicativo possui 17 telas em Jetpack Compose e persiste dados locais com Room. A experiência é navegável, mas ainda não consome o backend real. Autenticação, usuários, fotógrafos e orçamentos locais servem como protótipo e não representam a fonte de verdade de produção.

## Fase 1 — fundação Android

- [ ] Validar build limpo, lint, testes e instalação em emulador.
- [ ] Substituir testes de exemplo por uma linha de base útil.
- [ ] Criar CI para build, lint e testes.
- [ ] Definir pacote definitivo, flavors/ambientes e versionamento.
- [ ] Separar melhor navegação, domínio, dados locais e dados remotos.
- [ ] Adotar Navigation Compose e preparar deep links.

## Fase 2 — contrato e paridade

- [ ] Criar `docs/MATRIZ_PARIDADE.md` comparando app, web e API.
- [ ] Aprovar o escopo do primeiro lançamento.
- [ ] Definir autenticação móvel revogável com o backend.
- [ ] Definir cliente HTTP, serialização, erros e compatibilidade da API.
- [ ] Definir cache/offline sem duplicar regras de negócio.

## Fase 3 — integração funcional

- [ ] Autenticação, sessão, verificação de e-mail e conta.
- [ ] Descoberta e perfis públicos.
- [ ] Perfil profissional e portfólio.
- [ ] Orçamentos e propostas.
- [ ] Avaliações.
- [ ] Conversas, mensagens e notificações.
- [ ] Exportação e exclusão LGPD.
- [ ] Remover credenciais e autenticação locais de demonstração da release.

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

- [ ] Conta de desenvolvedor e pacote definitivo.
- [ ] Upload key protegida e Play App Signing.
- [ ] AAB de release e materiais PT-BR/EN-US.
- [ ] Faixa interna.
- [ ] Teste fechado e correções do pre-launch report.
- [ ] Lançamento gradual com monitoramento e plano de interrupção.

## Próxima entrega

Executar a Fase 1 e criar a primeira versão de `docs/MATRIZ_PARIDADE.md`. Nenhuma conexão com produção deve começar antes da decisão do contrato de autenticação móvel.

