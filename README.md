<p align="center">
  <img src="assets/hero.jpg" alt="Lens Click — banner com câmera e luz dourada sobre fundo escuro" width="100%" />
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-2.2.10-7F52FF" alt="Kotlin 2.2.10" />
  <img src="https://img.shields.io/badge/Jetpack%20Compose-BOM%202024.09-3DDC84" alt="Jetpack Compose" />
  <img src="https://img.shields.io/badge/Material%203-UI-37474F" alt="Material 3" />
  <img src="https://img.shields.io/badge/minSdk-24-orange" alt="minSdk 24" />
  <img src="https://img.shields.io/badge/AGP-9.1.1-4B8BBE" alt="Android Gradle Plugin 9.1.1" />
</p>

# 📸 Lens Click

> Conectando momentos a fotógrafos incríveis.

O **Lens Click** é o aplicativo Android da plataforma LensClick — um ponto de encontro entre quem quer eternizar momentos e fotógrafos profissionais. Este repositório contém o app em **Kotlin + Jetpack Compose (Material 3)**, com interface 100% em português do Brasil e visual inspirado em fotografia editorial: papel, tinta e toques de dourado.

---

## ✨ Funcionalidades

| Área | O que o app faz |
|---|---|
| 🪪 **Onboarding** | Tela de boas-vindas em tela cheia com fotografia, gradiente escuro e a marca desenhada à mão (Canvas) |
| 🔐 **Login & Cadastro** | Fluxo de autenticação simulado com validações (senha × confirmação, aceite dos Termos) e recuperação de senha |
| 🏠 **Início** | Localização, busca rápida, categorias (Casamento, Ensaio, Eventos, Infantil, Corporativo), fotógrafos em destaque e portfólios em alta |
| 🔎 **Descobrir** | Busca por nome/estilo, filtros por categoria, favoritos e contador de resultados |
| 👤 **Perfil do fotógrafo** | Bio, especialidades, estatísticas (experiência, ensaios, satisfação), avaliações, portfólio e preço "a partir de" |
| 💰 **Solicitar orçamento** | Formulário completo: tipo, data, local, duração, descrição e informações adicionais |
| 📋 **Meus orçamentos** | Abas *Solicitações · Propostas · Favoritos* e cartões expansíveis com status (Aguardando, Proposta recebida, Aceito, Recusado) |
| 💬 **Conversas** | Lista de conversas com busca, indicador de online, chat com balões e envio de mensagens |
| 👤 **Conta** | Perfil do usuário, menu de ações (dados, segurança, notificações, ajuda, sobre) e sair da conta |
| 🧭 **Navegação** | Barra inferior flutuante com 5 abas e fluxo por estado entre as **11 telas** do app |

## 🗺️ Fluxo de telas

```
Onboarding ⇄ Login ⇄ Cadastro
      └──────▶ Início ──▶ Descobrir ──▶ Perfil do fotógrafo ──▶ Solicitar orçamento
                 │                                            └─▶ Meus orçamentos
                 ├──▶ Conversas ──▶ Chat
                 └──▶ Conta
```

A navegação é uma máquina de estados simples e declarativa (`enum Screen` + `rememberSaveable`), sem dependências externas de rotas.

## 🧱 Stack e arquitetura

| Componente | Tecnologia |
|---|---|
| Linguagem | [Kotlin](https://kotlinlang.org) 2.2.10 |
| UI | [Jetpack Compose](https://developer.android.com/compose) + [Material 3](https://m3.material.io) (BOM `2024.09.00`) |
| Build | Gradle 9.3.1 (wrapper) + Android Gradle Plugin 9.1.1 |
| SDKs | `compileSdk` 36 · `minSdk` 24 (Android 7.0+) · `targetSdk` 36 |
| JDK | Toolchain 21 (provisionado automaticamente via Foojay) |
| Dados | Modelos e dados de exemplo locais (`LensClickData.kt`) — sem backend nesta versão |
| Empacotamento | Aplicativo single-module (`:app`) · `applicationId com.example.lensclickapp` · versão 1.0.0 |

## 🗂️ Estrutura do projeto

```
LensClickApp/
├── app/
│   ├── build.gradle.kts              # configuração do módulo Android
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── java/com/example/lensclickapp/
│       │   │   ├── MainActivity.kt   # Activity única (edge-to-edge + tema)
│       │   │   ├── data/
│       │   │   │   └── LensClickData.kt   # modelos Photographer/Budget + mock
│       │   │   └── ui/
│       │   │       ├── LensClickApp.kt    # as 11 telas + navegação por estado
│       │   │       └── theme/             # Color, Type e LensClickAppTheme (M3)
│       │   └── res/                  # ícones, imagem de onboarding e strings (pt-BR)
│       ├── androidTest/              # testes instrumentados (exemplo)
│       └── test/                     # testes de unidade (exemplo)
├── gradle/
│   ├── libs.versions.toml            # catálogo central de versões
│   └── wrapper/                      # Gradle 9.3.1
├── build.gradle.kts                  # build raiz
├── settings.gradle.kts               # módulos e repositórios
└── gradlew / gradlew.bat             # wrapper multiplataforma
```

## 🚀 Como executar

### Pré-requisitos

- **Android Studio** recente (com suporte a AGP 9.x) — ou apenas um JDK 17+ na CLI
- **SDK Platform 36** instalado no Android SDK Manager

### Pelo Android Studio

1. **File ▸ Open** e selecione a pasta do projeto;
2. Aguarde o *Gradle Sync* terminar;
3. Conecte um aparelho (ou abra um emulador) e clique em **Run ▶**.

### Pela linha de comando

```bash
# gera o APK de debug
./gradlew assembleDebug
# APK gerado em: app/build/outputs/apk/debug/app-debug.apk

# instala direto no aparelho/emulador conectado
./gradlew installDebug

# roda os testes de unidade
./gradlew testDebugUnitTest
```

> 💡 No Windows, use `gradlew.bat` no lugar de `./gradlew`.

## 🎨 Identidade visual

O design do app segue uma direção **editorial e atemporal**, inspirada no universo da fotografia analógica:

| Cor | Hex | Uso |
|---|---|---|
| 🖤 Tinta (*Ink*) | `#11110F` | Fundos fortes, botões primários e texto principal |
| 🤍 Papel | `#F6F3EB` | Fundo geral do app |
| ⚪ Branco | `#FFFEFA` | Superfícies elevadas (cartões, campos) |
| 🩶 Linha | `#E2DDD2` | Bordas e divisórias |
| 🩶 Cinza | `#716D64` | Texto secundário e placeholders |
| 🟡 Dourado | `#9A6A24` | Destaques, avaliações e preços |

Detalhes que fazem a diferença: o **logotipo** (uma lente com abertura estilizada) é desenhado via `Canvas` no próprio código, os cantos são arredondados e suaves, e a tipografia usa pesos semibold com espaçamento de letras marcante — tudo para reforçar um clima sofisticado de estúdio fotográfico.

## 📌 Status do projeto

Esta versão é uma **interface completa e navegável** com dados de exemplo: onboarding, autenticação, descoberta de fotógrafos, orçamentos, conversas e conta funcionam ponta a ponta no cliente, usando os modelos e catálogos locais de `LensClickData.kt`.

Como próximo passo natural, a UI está pronta para ser conectada ao **backend da plataforma LensClick** — autenticação real, fotógrafos, orçamentos e mensagens em tempo real.

## 📄 Licença

Este repositório ainda não possui licença declarada. Consulte os mantenedores antes de reutilizar qualquer parte do código.

---

<p align="center">
  Feito com ❤️ e uma boa lente no Brasil · <b>Lens Click</b> 📷
</p>
