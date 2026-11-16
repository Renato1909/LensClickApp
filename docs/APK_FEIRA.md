# APK da Feira Tecnológica (sideload)

Este documento descreve como gerar e instalar o APK release assinado do **LensClickApp** para a demonstração da Feira Tecnológica.

## Configuração do app (release)

- **Backend (fonte de verdade):** `https://lensclick.renato-aparecido-business.workers.dev` (produção)
- **Build type:** `release` (`com.lensclick.app`, sem sufixo de aplicação)
- **Distribuição:** sideload (APK instalado diretamente no aparelho) — **não** Google Play nesta fase
- **Assinatura:** release assinado com keystore local/segredo de CI (nunca versionado no Git)

A URL de produção e o `applicationId` estão definidos em `app/build.gradle.kts` no `buildType` `release`.

## Opção A — Gerar pelo GitHub Actions (recomendado)

> **Observação importante:** adicionar/alterar `.github/workflows/android.yml` no GitHub exige credenciais Git com escopo `workflow`. Se o push do workflow for rejeitado, aplique o job abaixo em `.github/workflows/android.yml` a partir de um ambiente autorizado (ou via interface do GitHub) e faça push com permissão de workflow.

O job **"APK release da feira"** (executado manualmente via `workflow_dispatch`) gera `:app:assembleRelease` e publica o artefato `lensclick-feira-release-apk`. Adicione este job ao final de `.github/workflows/android.yml`:

```yaml
  release:
    name: APK release da feira
    if: github.event_name == 'workflow_dispatch'
    runs-on: ubuntu-24.04
    timeout-minutes: 30
    env:
      LENSCLICK_RELEASE_KEYSTORE_BASE64: ${{ secrets.ANDROID_KEYSTORE_BASE64 }}
      LENSCLICK_RELEASE_STORE_PASSWORD: ${{ secrets.ANDROID_KEYSTORE_PASSWORD }}
      LENSCLICK_RELEASE_KEY_ALIAS: ${{ secrets.ANDROID_KEY_ALIAS }}
      LENSCLICK_RELEASE_KEY_PASSWORD: ${{ secrets.ANDROID_KEY_PASSWORD }}
    steps:
      - uses: actions/checkout@11d5960a326750d5838078e36cf38b85af677262 # v4
        with:
          persist-credentials: false
      - uses: actions/setup-java@cf277c60eb25467037889841efdb72551f06f6c3 # v4
        with:
          distribution: temurin
          java-version: '21'
      - uses: gradle/actions/setup-gradle@0b6dd653ba04f4f93bf581ec31e66cbd7dcb644d # v4
      - uses: android-actions/setup-android@be39fa834029ff78f1a44aa3bb0819b8fc2bd8fd # v4.0.4
      - name: Instalar SDK exigido pelo projeto
        run: sdkmanager 'platforms;android-36.1' 'build-tools;36.0.0' 'platform-tools'
      - name: Verificar segredos de assinatura
        run: |
          if [ -z "${LENSCLICK_RELEASE_KEYSTORE_BASE64}" ] || [ -z "${LENSCLICK_RELEASE_STORE_PASSWORD}" ] || [ -z "${LENSCLICK_RELEASE_KEY_ALIAS}" ] || [ -z "${LENSCLICK_RELEASE_KEY_PASSWORD}" ]; then
            echo "Segredos de assinatura ausentes. Configure ANDROID_KEYSTORE_BASE64, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS e ANDROID_KEY_PASSWORD para gerar o APK release assinado."
            exit 1
          fi
      - name: Gerar APK release assinado
        run: bash ./gradlew --no-daemon --stacktrace :app:assembleRelease
      - name: Publicar APK release
        uses: actions/upload-artifact@ea165f8d65b6e75b540449e92b4886f43607fa02 # v4
        with:
          name: lensclick-feira-release-apk
          retention-days: 30
          path: app/build/outputs/apk/release/*.apk
          if-no-files-found: error
```

### 1) Criar a keystore de assinatura (uma vez, localmente)

Se ainda não existir uma chave de release, gere uma keystore **e guarde-a com segurança fora do repositório**:

```bash
keytool -genkeypair -v \
  -keystore lensclick-release.jks \
  -alias lensclick \
  -keyalg RSA -keysize 2048 -validity 10000
```

> Nunca faça commit de `.jks`/`.keystore`, senhas ou tokens. Faça backup seguro da keystore e das senhas: sem elas não é possível atualizar um app assinado com a mesma chave.

### 2) Configurar segredos no GitHub (repositório LensClickApp)

Em **Settings → Secrets and variables → Actions**, crie:

| Secret | Conteúdo |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Keystore codificada em Base64 (arquivo inteiro) |
| `ANDROID_KEYSTORE_PASSWORD` | Senha da keystore (`storePassword`) |
| `ANDROID_KEY_ALIAS` | Alias da chave (ex.: `lensclick`) |
| `ANDROID_KEY_PASSWORD` | Senha da chave (`keyPassword`) |

Para gerar o Base64 (Linux):

```bash
base64 -w 0 lensclick-release.jks > lensclick-release.b64.txt
```

Copie o conteúdo de `lensclick-release.b64.txt` para o secret `ANDROID_KEYSTORE_BASE64` e apague o arquivo temporário se desejar.

### 3) Executar o workflow e baixar o APK

1. No GitHub, abra **Actions → Android → Run workflow** (branch `main`).
2. Aguarde o job **"APK release da feira"** concluir com sucesso.
3. Baixe o artefato **`lensclick-feira-release-apk`** (contém `app-release.apk`).

> Observação: o job de release exige os segredos acima; sem eles ele falha com mensagem orientativa. Os jobs de build/testes instrumentados continuam independentes.

## Opção B — Gerar localmente (Android Studio / Gradle)

Requer Android SDK instalado (ver `docs/AMBIENTE_ANDROID.md`).

1. **Android Studio:** *Build → Generate Signed Bundle / APK → APK*, selecione a keystore/alias e o build variant `release`.
2. **Linha de comando (Gradle):** informe a assinatura por variáveis de ambiente ou propriedades Gradle (não versionar):

```bash
export LENSCLICK_RELEASE_KEYSTORE_PATH="/caminho/seguro/lensclick-release.jks"
export LENSCLICK_RELEASE_STORE_PASSWORD="***"
export LENSCLICK_RELEASE_KEY_ALIAS="lensclick"
export LENSCLICK_RELEASE_KEY_PASSWORD="***"

./gradlew :app:assembleRelease
```

Saída: `app/build/outputs/apk/release/app-release.apk` (assinado quando as variáveis estão presentes).

Alternativa: passar como propriedades Gradle `-Plensclick.releaseKeystorePath=...`, `-Plensclick.releaseStorePassword=...`, `-Plensclick.releaseKeyAlias=...`, `-Plensclick.releaseKeyPassword=...` ou `-Plensclick.releaseKeystoreBase64=...` (evite expor em histórico de shell).

## Instalação por sideload (aparelho Android)

1. Transfira o `app-release.apk` para o aparelho (USB, Google Drive, e-mail, etc.).
2. No aparelho, permita instalação de **apps de fontes desconhecidas** para o app usado para abrir o APK (Arquivos/Chrome), se solicitado.
3. Abra o APK e confirme a instalação.
4. Certifique-se de que o aparelho tenha acesso à internet para falar com a API de produção.
5. (Opcional) Se houver uma versão anterior com assinatura diferente instalada, desinstale-a antes de instalar a nova.

## Checklist rápido para a demo

- [ ] APK release **assinado** gerado (artefato `lensclick-feira-release-apk` ou `app-release.apk` local).
- [ ] Instala e abre em aparelho real.
- [ ] Aponta para produção (`https://lensclick.renato-aparecido-business.workers.dev`).
- [ ] Contas de demonstração (cliente e fotógrafo) criadas/populadas no ambiente de produção.

## Segurança

- **Nunca** versionar keystore, senhas, tokens ou `local.properties` com segredos.
- `app/build/signing/` (keystore decodificada em build) é gerado em `build/` e já é ignorado; ainda assim, não copie keystores para o repositório.
- Use contas de demonstração na feira; evite expor dados reais e não registre tokens em logs/capturas.
