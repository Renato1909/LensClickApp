# LensClick — App Android (cliente)

App Android nativo (Kotlin + Jetpack Compose) para o marketplace de fotógrafos
[LensClick](https://lensclick.renato-aparecido-business.workers.dev/).

> Projeto acadêmico da ETEC Zona Leste em parceria com a IBM Brasil.

## Requisitos

- Android Studio **Hedgehog** ou mais recente (qualquer distribuição — *Canary/Iguana* ok)
- Android Gradle Plugin 8.x / Kotlin 2.0.20 (plugin e versões no `build.gradle.kts`)
- SDK 34 (compile), API 26+ (min), API 34 (target)
- JDK 17

Não há `gradlew` neste repositório por questões de licença. Abra **directamente no
Android Studio**: `File → Open...` → selecione a pasta raiz do projeto
(`LensClickApp/`). O Studio fará o download do AGP/Kotlin plugins automaticamente.

## Arquitetura

- **UI:** Jetpack Compose (Material 3)
- **Armazenamento local:** `SessionManager` + `SharedPrefsCookiesStorage` persistindo
  cookies de sessão (compatível com o Cloudflare Workers que usa cookie-based auth)
- **Remote:** `ApiClient.kt` (Ktor) com `ContentNegotiation` via `kotlinx.serialization`
- **Padrão:** MVVM + `StateFlow`

### Telas principais

| Tela                  | Package                          |
|-----------------------|----------------------------------|
| Splash / Login        | `ui/screens/auth`                |
| Busca de fotógrafos   | `ui/screens/search`              |
| Perfil do fotógrafo   | `ui/screens/search/PhotographerProfileScreen.kt` |
| Orçamentos          | `ui/screens/quotes`              |
| Minha conta         | `ui/screens/account`            |
| Mensagens (conversas)| `ui/screens/messages`            |
| Thread de mensagens | `ui/screens/messages/ThreadScreen.kt` |

### Navegação

`ui/navigation/LensClickNavHost.kt` — single-activity, Navigation Compose.
Rotas principais (em `Routes`):

```
splash, login, register,
photographer/{slug},
quotes, request-quote/{slug},
account, edit-profile, change-password, professional-profile, delete-account,
conversations, thread/{userId}/{userName}
```

## Build & Run

1. Clone ou abra no Android Studio
2. Se o projeto pedir, permita o download do SDK/AGP
3. **Run** → escolha um emulador Android 8.0 (API 26)+ ou dispositivo físico
4. O app aponta para produção por padrão. Ver `gradle.properties` → `BASE_URL`.

## API

Veja `API_CONTRACT.md` na raiz para o contrato completo das rotas REST.

## License

Uso acadêmico — não é para redistribuição comercial.
