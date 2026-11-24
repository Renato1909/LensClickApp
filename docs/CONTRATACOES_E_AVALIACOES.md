# Contratações, avaliações, orçamentos e idiomas

## Fluxos

- **Configurações → Idioma** troca o idioma na hora. O app guarda a preferência no aparelho, inclusive entre sessões da conta. Os catálogos `app/src/main/assets/i18n/en.json` e `es.json` usam o texto português como chave e preservam os parâmetros `{0}`, `{1}` etc. Mensagens, descrições e comentários escritos pelos usuários continuam no idioma em que foram enviados.
- O cliente encontra **Contratar** e **Solicitar orçamento** no topo do portfólio. **Contratar** abre os dados do evento quando há preço publicado e abre o pedido de orçamento quando o preço depende da proposta. Após aceitar a proposta em **Orçamentos → Ver detalhes**, o cliente pode contratar com o valor combinado. O fluxo segue **Dados do evento → Pagamento → Revisão**, com o total antes da confirmação. O início e o fim ficam na data selecionada e seguem o horário de Brasília (UTC−3). O app usa o preço inicial publicado no perfil para as contratações diretas e o preço da proposta nas contratações com orçamento. O valor é validado no servidor e fica registrado no pedido.
- No portfólio, **Favoritar** salva o fotógrafo em **Orçamentos → Favoritos**. **Conversar** reabre o chat da conta ou permite enviar a primeira mensagem. O formulário de orçamento usa o calendário para escolher e confirmar a data do evento.
- O fotógrafo aceita ou recusa em **Pedidos → Contratações recebidas**. Depois do horário de término, pode informar que realizou o serviço. O cliente confirma a conclusão em **Orçamentos → Contratações**. As mudanças de status e pagamento ficam no histórico dos participantes. O backend impede a aceitação de horários sobrepostos.
- O cliente pode avaliar uma contratação concluída pelo pedido ou pela seção de avaliações do portfólio. O servidor exige uma contratação por avaliação e valida autoria, nota e comentário. As avaliações existentes continuam públicas e o autor mantém as operações de edição e exclusão disponíveis pelo site. O histórico de contatos continua acessível.
- O ranking é exibido em linha no início e na busca. Ele usa uma média bayesiana com cinco avaliações de referência de nota 4, desconta 0,10 ponto por unidade de variância e desempata pela quantidade de avaliações e pelo slug. As consultas do ranking e das avaliações refletem os registros publicados a cada acesso. Rascunhos, solicitações de contato e orçamentos de demonstração ficam fora do cálculo.
- **Orçamentos de demonstração** reúne cinco exemplos fictícios. Use **Cliente** e **Fotógrafo** para testar envio de proposta, aprovação, recusa e conclusão. O histórico fica no aparelho e não é enviado ao backend.

## Banco e rotas

O backend fica no checkout `LensClick-main`. O [patch do backend](../backend/lensclick-engagements.patch) registra as mudanças correspondentes do Worker, da página web de avaliações, do esquema e dos testes. A migration `0019_engagements.sql` acrescenta `engagements`, `engagement_events`, índices, gatilhos de histórico e um vínculo único e opcional de `review_requests` para `engagements`. Ela preserva os registros anteriores. A exportação da conta inclui as contratações e seu histórico.

| Método | Rota | Ação |
| --- | --- | --- |
| POST | `/api/photographers/:slug/engagements` | Registrar contratação com preço validado e chave de idempotência |
| GET | `/api/account/engagements` | Listar contratações da conta |
| GET | `/api/account/engagements/:id` | Consultar o pedido e seu histórico |
| PUT | `/api/account/engagements/:id` | Alterar status ou confirmar recebimento de dinheiro |
| POST | `/api/account/engagements/:id/review` | Publicar a avaliação da contratação concluída |
| PUT | `/api/account/budget-requests/:id` | Recusar orçamento como fotógrafo, usando a ação `decline` |
| GET | `/api/photographers?sort=rating_rank` | Ler o ranking e as prévias de portfólio |
| GET | `/api/photographers/:slug/reviews` | Ler os comentários recentes e a distribuição de todas as notas |

Para um novo checkout, use `git apply --check` antes de aplicar o patch e execute `npm run db:migrate:local` no backend com os dados de desenvolvimento. O APK de debug usa o Worker do host do emulador na porta `8787`. Para a distribuição pública, o backend e a migração precisam estar disponíveis no ambiente remoto antes da liberação do novo APK.

## Pagamentos

Dinheiro é pago fora do app. Apenas o fotógrafo do pedido pode registrar que recebeu o valor. As taxas da plataforma estão em R$ 0,00 nesta versão.

PIX, cartão de crédito e cartão de débito registram a modalidade e exibem **Aguardando integração de pagamento**. Para ativar cobranças, será necessário configurar um provedor no backend e adicionar sessões de pagamento, referência externa, taxas e confirmação por webhook autenticado e idempotente. O Android receberá o QR/código PIX ou o checkout seguro do provedor e acompanhará o status retornado pelo backend. Nenhuma chave PIX, número de cartão ou credencial de cobrança é solicitado pelo fluxo atual.

## Verificação

- Backend com Node 22. Executar `node --test` e `npm run lint` no checkout do Worker. A suíte inclui concorrência na publicação de avaliação, permissões, idempotência, uso da proposta aceita, proteção de horários, estados de pagamento e ranking.
- Android. Executar `./gradlew :app:testDebugUnitTest :app:lintDebug :app:connectedDebugAndroidTest`. Os testes de interface cobrem navegação, troca e persistência de idioma, formulário de contratação, orçamentos fictícios e abertura do ranking e das avaliações.

## Arquivos alterados

No aplicativo, sob `app/src/main/java/com/lensclick/app`:

- Inicialização em `LensClickApplication.kt` e catálogo em `ui/AppLanguage.kt`. Traduções em `app/src/main/assets/i18n/en.json` e `app/src/main/assets/i18n/es.json`.
- Interface e navegação em `ui/AccountSettings.kt`, `ui/LensClickApp.kt`, `ui/LensClickViewModel.kt`, `ui/DiscoveryScreens.kt`, `ui/DemoBudgetsScreen.kt` e `ui/EngagementScreens.kt`.
- Modelos em `domain/Budgets.kt`, `domain/Engagement.kt`, `domain/PublicPhotographers.kt` e `domain/Reviews.kt`.
- Integrações em `data/remote/Budgets.kt`, `data/remote/Conversations.kt`, `data/remote/Engagement.kt`, `data/remote/PublicPhotographers.kt`, `data/remote/Reviews.kt` e `data/local/FairPhotographers.kt`.
- Testes em `app/src/androidTest/java/com/lensclick/app/ui`. Foram atualizados `AccountSettingsNavigationTest.kt`, `LensClickNavigationTest.kt` e `PhotographerRankingTest.kt`, e criado `HireFormTest.kt`.

No backend, o patch contém `src/account.mjs`, `src/budgets.mjs`, `src/engagements.mjs`, `src/photographers.mjs`, `src/router.mjs`, `src/worker.mjs`, `db/schema.ts`, `migrations/0019_engagements.sql`, `public/fotografo.html`, `public/reputation.js`, `public/locales/en-US.json`, `public/locales/pt-BR.json` e quatro arquivos de testes. Os testes são `test/android-contract-alignment.test.mjs`, `test/d1-health.test.mjs`, `test/engagements.test.mjs` e `test/reviews.test.mjs`.
