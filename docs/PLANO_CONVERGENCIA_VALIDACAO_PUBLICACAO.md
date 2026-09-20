# Plano de convergência, validação e publicação do Android

> Plano operacional do aplicativo LensClick nativo em Kotlin, desenvolvido no Android Studio.
> A plataforma web e a API permanecem no repositório `Renato1909/LensClick`.

## 1. Estado confirmado

- projeto Android versionado em `Renato1909/LensClickApp`;
- Kotlin 2.2.10, Jetpack Compose, Material 3 e Room;
- módulo único `:app`, `minSdk 24`, `targetSdk 36` e `compileSdk 36`;
- 17 telas navegáveis para clientes e fotógrafos;
- autenticação, perfis, orçamentos e demais dados ainda são locais;
- pacote atual `com.example.lensclickapp`, provisório para publicação;
- backend da plataforma ainda não está conectado;
- testes existentes são apenas os exemplos iniciais do projeto.

O objetivo é transformar a interface local em um cliente seguro da mesma plataforma LensClick, sem duplicar usuários, regras de negócio ou banco de produção.

## 2. Princípios

1. A API do repositório `LensClick` é a fonte de verdade de autenticação, autorização e dados.
2. Nenhuma senha real será persistida ou validada pelo Room.
3. Tokens, chaves de assinatura e credenciais nunca entram no Git.
4. Cada integração será entregue como uma fatia vertical pequena, testável e reversível.
5. A publicação seguirá faixa interna, teste fechado e lançamento gradual.
6. Diferenças entre web e Android serão deliberadas e registradas, nunca acidentais.

## 3. Gates de execução

### Gate A — diagnóstico e linha de base

- confirmar build limpo, testes e instalação de debug;
- inventariar arquitetura, entidades, telas, recursos e débitos;
- definir o `applicationId` definitivo antes do primeiro envio à Google Play;
- decidir ambientes de API, configuração de debug/release e política de versões;
- adicionar CI para build, lint e testes Android.

**Saída:** projeto reproduzível e linha de base técnica registrada.

### Gate B — matriz de paridade

Manter `docs/MATRIZ_PARIDADE.md` com: capacidade, API, web, Android atual, decisão, prioridade, dependências, testes e estado.

O inventário deve cobrir cadastro, login, logout, recuperação e verificação de e-mail; conta e LGPD; descoberta e perfil público; perfil profissional e portfólio; avaliações; orçamentos/propostas; conversas e mensagens; notificações; internacionalização; acessibilidade; estados de erro/offline; Termos e Privacidade.

Cada item será classificado como **obrigatório no primeiro lançamento**, **equivalente nativo**, **web-only justificado** ou **adiado com risco aceito**.

### Gate C — contrato móvel seguro

O backend atual usa sessão web por cookie. Antes de conectar o aplicativo, os dois repositórios devem aprovar um contrato móvel revogável que cubra:

- login, renovação, expiração, logout e revogação;
- armazenamento protegido por Android Keystore;
- revogação após troca de senha e limite de sessões;
- distinção entre proteção CSRF da web e autenticação nativa;
- compatibilidade de versões da API;
- telemetria sem dados pessoais ou segredos.

**Saída:** decisão arquitetural e contrato coberto por testes no backend e no Android.

### Gate D — integração por fatias

Ordem recomendada:

1. autenticação, sessão e conta;
2. descoberta e perfis públicos;
3. perfil profissional e portfólio;
4. orçamentos e propostas;
5. avaliações;
6. conversas, mensagens e notificações;
7. exportação e exclusão LGPD;
8. recursos administrativos apenas se aprovados para celular.

Em cada fatia, substituir mocks por repositórios remotos, manter cache local somente quando adequado e incluir carregamento, vazio, erro, offline, acessibilidade e testes.

### Gate E — verificação em duas etapas

2FA será um recurso único da conta, válido na web e no Android. Antes da implementação, produzir threat model e decisão técnica comparando TOTP, passkeys e códigos por e-mail.

A recomendação inicial é TOTP com códigos de recuperação, incluindo ativação com reautenticação, confirmação do primeiro código, recuperação, dispositivo confiável com expiração, rate limiting, auditoria e revogação de sessões. Segredos TOTP e códigos de recuperação não podem aparecer em logs nem permanecer expostos após a criação.

### Gate F — validação integral

| Camada | Evidência mínima |
|---|---|
| Kotlin | testes JVM de regras, mapeadores, ViewModels e repositórios |
| Android | testes instrumentados dos fluxos críticos e armazenamento seguro |
| Contrato | testes contra ambiente controlado da API |
| Compatibilidade | Android mínimo, alvo e aparelhos/tamanhos representativos |
| Dispositivo real | telefone e tablet; rotação, background e rede instável |
| Segurança | sessão, 2FA, logs, links profundos e telas sensíveis |
| Acessibilidade | TalkBack, foco, contraste, fonte ampliada e alvos de toque |
| Privacidade | coleta real coerente com Termos, Política e Data safety |
| Release | AAB assinado, instalação e smoke da variante de produção |

Referências: [estratégias de teste](https://developer.android.com/training/testing/fundamentals/strategies), [o que testar](https://developer.android.com/training/testing/fundamentals/what-to-test) e [preparação para release](https://developer.android.com/studio/publish/preparing).

### Gate G — domínio, e-mail e revisão jurídica

São dependências compartilhadas com o repositório da plataforma:

- escolher e registrar o domínio;
- configurar Cloudflare, certificado, URL canônica e redirecionamentos;
- autenticar o domínio no Brevo com DKIM e DMARC;
- atualizar URLs da API, links profundos, suporte e Política de Privacidade;
- submeter Termos, Política, mapa de dados e respostas de Data safety à revisão jurídica formal;
- versionar os textos aprovados e suas datas de vigência.

O aplicativo não deve inventar domínio, identidade legal ou respostas de privacidade antes dessas decisões.

### Gate H — Google Play

1. definir conta de desenvolvedor, responsáveis e pacote definitivo;
2. proteger a upload key e ativar Play App Signing;
3. gerar AAB de release e materiais da loja;
4. preencher App content e Data safety a partir da auditoria real;
5. publicar na faixa interna e depois no teste fechado aplicável à conta;
6. corrigir pre-launch report e feedback;
7. usar lançamento gradual e monitorar crashes, ANRs, autenticação e API.

Referências: [publicação Android](https://developer.android.com/studio/publish), [assinatura do app](https://developer.android.com/studio/publish/app-signing), [criação no Play Console](https://support.google.com/googleplay/android-developer/answer/9859152?hl=pt-br) e [publicação de versão](https://support.google.com/googleplay/android-developer/answer/9859751?hl=pt-br).

## 4. Ordem executiva

1. Gate A — linha de base e CI.
2. Gate B — matriz real de paridade.
3. Gate C — contrato móvel em conjunto com o backend.
4. Gate G — domínio e revisão jurídica em paralelo.
5. Gate D — integração por fatias.
6. Gate E — 2FA multicliente.
7. Gate F — validação integral.
8. Gate H — faixas de teste e lançamento gradual.

## 5. Decisões pendentes do mantenedor

- `applicationId` definitivo;
- domínio e titularidade;
- conta Google Play pessoal ou de organização;
- escopo obrigatório do primeiro lançamento;
- presença ou não de funções administrativas no aplicativo;
- política final de 2FA e recuperação;
- responsável jurídico e identidade pública do controlador.

