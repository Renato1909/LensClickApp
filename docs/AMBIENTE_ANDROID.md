# Ambiente Android e validação reproduzível

## Diagnóstico local em 19/09/2026 (America/Sao_Paulo)

- Windows 11; árvore inicial limpa na main `bd3c140`, igual a `origin/main` após fetch. PR #3 integrado; nenhum workflow anterior.
- Java do PATH: Oracle 26.0.2. O Gradle 9.3.1 e um Temurin 21.0.7 já estavam em cache. O projeto exige daemon Java 21 em `gradle/gradle-daemon-jvm.properties`; a versão Kotlin mostrada por `gradlew --version` é a interna do Gradle, não a do app.
- `JAVA_HOME`, `ANDROID_HOME` e `ANDROID_SDK_ROOT` não definidos; `local.properties` ausente.
- `adb`, `sdkmanager` e `emulator` ausentes do PATH; SDK e Android Studio não encontrados nos diretórios convencionais consultados. Não foi feita varredura de discos inteiros; uma instalação em caminho personalizado ainda pode ser indicada pelo mantenedor.
- `gradlew.bat --version --offline`: **passou**.
- `gradlew.bat --offline --no-daemon :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest`: **bloqueado**, antes da compilação, por `SDK location not found`.
- Sem SDK/ADB disponível, instalação e testes instrumentados locais não puderam ser executados. Isso não prova ausência de telefone conectado por USB.
- Nenhum SDK, emulador ou componente grande foi instalado localmente; nenhuma configuração global foi alterada.

## Requisitos exatos

O `compileSdk` usa API **36 com minorApiLevel 1**: instalar `platforms;android-36.1`, e não somente `android-36`. `targetSdk` continua 36, `minSdk` 24. AGP 9.1.1 usa Build Tools 36.0.0; wrapper 9.3.1 e daemon JDK 21 estão fixados no projeto. Consulte a [compatibilidade oficial do AGP](https://developer.android.com/build/releases/agp-9-1-0-release-notes).

Após autorização para instalar componentes locais, usar Android Studio compatível com AGP 9.1 ou Android command-line tools. No SDK Manager instalar:

- Android SDK Platform 36.1;
- Android SDK Build-Tools 36.0.0;
- Android SDK Platform-Tools;
- para emulador: Android Emulator e imagem x86_64 `google_apis` API 24 e/ou 36, com virtualização disponível.

Apontar `ANDROID_HOME` para o SDK **existente**, ou definir `sdk.dir` em `local.properties` ignorado pelo Git. Configurar Gradle JDK 21 no Android Studio; não usar o Java 26 do PATH como daemon. Aceitar licenças pelo SDK Manager conforme a política do mantenedor. Não copiar arquivos de outra máquina nem versionar caminhos pessoais.

## Comandos locais

Neste ambiente, todos os comandos passam pelo RTK. `rtk proxy` preserva os comandos e evita a falha do filtro `rtk git` quando não encontra configuração HOME/Claude.

```powershell
rtk proxy .\gradlew.bat --version
rtk proxy .\gradlew.bat --no-daemon clean :app:assembleDebug :app:testDebugUnitTest :app:lintDebug :app:assembleDebugAndroidTest
rtk proxy adb devices -l
rtk proxy .\gradlew.bat :app:installDebug :app:connectedDebugAndroidTest
```

O APK é gerado em `app/build/outputs/apk/debug/app-debug.apk`. Relatórios: `app/build/reports/tests/testDebugUnitTest/`, `app/build/reports/lint-results-debug.html` e `app/build/reports/androidTests/connected/`. `--offline` só funciona quando todas as dependências já estão em cache.

## CI

`.github/workflows/android.yml` executa build debug, testes JVM, lint e compilação dos testes instrumentados; jobs separados instalam o APK e executam instrumentados em emuladores API 24 e 36. Usa runners Linux descartáveis com SDK, JDK 21, actions fixadas por SHA, permissão somente leitura e relatórios retidos por 7 dias. Não usa credenciais de produção ou assinatura release. A instalação de SDK ocorre somente nos runners.

Erros de build/lint/testes impedem sucesso do job; não há `continue-on-error` nem baseline que esconda erros. Os artefatos são APKs de demonstração e relatórios. A branch main não possuía proteção na consulta inicial: o mantenedor deve configurar estes checks como obrigatórios se desejar imposição pelo GitHub.

O sucesso do CI não substitui validação em aparelho real, TalkBack, tablet, rede, restauração de processo e revisão de release. Os resultados e links de execução efetivamente obtidos devem ser registrados no handoff.
