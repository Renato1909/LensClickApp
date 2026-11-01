# Validação da primeira fatia de autenticação móvel

Data da validação: 31/10/2026  
Android: `0abfa31ff8b70dfee6622de8426b28b3c8109f74`  
Backend local: `1a0845baa3d229ead6b6f2b000a4136e6fc03b6b`

## Resultado executivo

A camada de dados Android, o ViewModel, a navegação de entrada/saída e os
contratos móveis do backend passaram as validações automatizadas disponíveis.
Isso não equivale a uma aprovação completa de release: não houve dispositivo
ou emulador conectado localmente, e alguns cenários de processo, acessibilidade
e rede real continuam sem execução local.

## Testes executados localmente

### Android

Com JDK 21 e SDK Android 36.1:

```text
:app:testDebugUnitTest
:app:compileDebugAndroidTestKotlin
:app:assembleDebugAndroidTest
:app:lintDebug
:app:assembleDebug
```

Resultado: todos concluídos com `BUILD SUCCESSFUL`.

Cobertura observada:

- login remoto, armazenamento da sessão e `/me`;
- logout com limpeza local;
- erro de credencial (`401`);
- expiração/revogação representada pelo estado de sessão expirada;
- estados de loading, sucesso e erro no ViewModel;
- mapeamento Android de `400`, `401`, `403`, `404`, `409`, `429`, `500` e
  `503`;
- navegação de entrada e retorno da descoberta pública;
- compilação dos testes instrumentados.

### Backend

No repositório `C:\Users\renat\Desktop\LensClick-backend-work`:

```text
npm test -- --test-name-pattern="mobile|auth|account|password|session|revok" ...
npm test
npm run lint
```

Resultados:

- contratos/auth focados: 21 testes aprovados;
- suíte completa: aprovada, código de saída 0;
- lint: aprovado;
- expiração móvel manual com fixture D1: aprovado; `/api/mobile/auth/me`
  retornou `401` após `expires_at = 0`;
- logout/revogação: cobertos pelos contratos móveis, com `/me` retornando
  `401` após o logout;
- login válido: `200`, token opaco e sem cookie;
- login inválido: `401` com resposta genérica;
- isolamento cookie web/token móvel: aprovado;
- limite de três sessões móveis: aprovado;
- revogação por troca de senha: aprovada nos testes de contrato.

`npm run check` não foi concluído localmente porque o executável `wrangler`
não estava disponível no ambiente (`'wrangler' is not recognized`).

## Matriz de cenários

| Cenário | Evidência | Resultado |
|---|---|---|
| Login válido | `mobile-auth-route-contracts.test.mjs` | Aprovado |
| Login inválido | contrato móvel e `MobileAuthRepositoryTest` | Aprovado |
| `401` | backend + mapeamento Android | Aprovado |
| `403` | mapeamento Android; contratos gerais de autorização | Aprovado no escopo disponível |
| `429` | mapeamento Android; limites backend | Aprovado no escopo disponível |
| `5xx` | mapeamento Android para `500`/`503` | Aprovado no escopo disponível |
| Expiração | fixture backend com sessão móvel expirada | Aprovado |
| Logout | contrato móvel + repositório Android | Aprovado |
| Revogação | logout, troca de senha e exclusão no backend | Aprovado |
| Reinício do processo | sem processo Android real local | Não executado |
| Rede indisponível | sem teste de transporte offline em dispositivo | Não executado |
| Troca de conta | sem teste instrumentado com duas contas reais | Não executado |
| Rotação de configuração | variantes/builds já compilados; sem endpoint staging operacional | Parcial |
| Acessibilidade básica | inspeção de semântica não automatizada | Parcial |
| Retorno de navegação | `LensClickNavigationTest` | Aprovado no CI |

## Testes executados no CI

O workflow Android do PR [#8](https://github.com/Renato1909/LensClickApp/pull/8)
executou o commit `0abfa31`:

- Build, lint e testes JVM: aprovado;
- instrumentados API 24: aprovado;
- instrumentados API 36: aprovado.

Esses jobs fornecem evidência de execução instrumentada em emuladores API 24
e API 36. Não substituem testes em aparelho físico, rede real, TalkBack,
restauração após encerramento forçado ou validação de produção.

## Limitações do ambiente local

- `adb` não estava disponível no `PATH`; o binário local iniciou o daemon, mas
  não havia nenhum dispositivo conectado;
- não foi possível executar `connectedDebugAndroidTest` localmente;
- não foi possível validar reinício real do processo, Android Keystore após
  encerramento, clipboard, screenshots ou troca de conta em UI;
- não havia endpoint staging operacional fornecido fora do Git;
- `wrangler` não estava disponível para o dry-run de deploy backend.

## Acessibilidade e navegação

Os testes instrumentados existentes validam a entrada na tela de login,
retorno da descoberta pública e ausência de acesso privado sem sessão. Não há
atualmente uma suíte dedicada para TalkBack, ordem de foco, tamanho de fonte,
contraste medido, ações de teclado ou descrição de todos os controles.

## Recomendação

Aceitar a fatia apenas para integração e revisão do PR, não como release de
produção. Antes do aceite de publicação, adicionar testes instrumentados de
reinício/troca de conta, um cenário offline controlado, validação de
acessibilidade e uma execução com endpoint staging real.
