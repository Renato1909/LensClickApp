# Roadmap do LensClick App

> Evolução do aplicativo Android conectado à plataforma LensClick.

## Estado Atual

O aplicativo possui 17 telas em Jetpack Compose com suporte a autenticação por Bearer no Android Keystore, descoberta pública, perfil profissional, portfólio com upload multipart, orçamentos, propostas, agenda, favoritos, conversas/mensagens, avaliações, exportação/exclusão LGPD e preparação 100% concluída no cliente para 2FA TOTP (ADR 002).

## Fase 1 — Fundação Android
- [x] Testes JVM base e repositório/ViewModel.
- [x] CI para build, lint e testes (`.github/workflows/android.yml`: build debug, testes JVM, lint, instrumentados em emuladores API 24/36).
- [x] Nenhuma senha salva no Room (migração 2→3).
- [x] Variantes de build (`debug`, `staging`, `release`).
- [x] Separação em `data.local`, `data.remote`, `data.remote.auth`, `domain` e `ui`.
- [x] Navegação declarativa com Navigation Compose e pilha de retorno.

## Fase 2 — Integração Funcional Remota
- [x] Autenticação, sessão e cadastro móvel via Bearer.
- [x] Descoberta, perfil público por slug e leitura de portfólio.
- [x] Edição de perfil profissional e gestão de portfólio com upload.
- [x] Orçamentos, propostas, agenda e favoritos integrados ao backend.
- [x] Avaliações verificadas (leitura e submissão).
- [x] Conversas e mensagens remotas.
- [x] Exportação e exclusão de conta (LGPD).

## Fase 3 — Segurança e 2FA
- [x] Proteção de credenciais de sessão no Android Keystore.
- [x] Implementação no Android do 2FA TOTP (ADR 002: desafio no login, QR/URI, códigos de recuperação e desativação).
- [ ] Deploy das rotas móveis de 2FA no Worker backend (`Renato1909/LensClick`).

## Fase 4 — Prontidão para Demo / Feira Tecnológica
- [x] Validação de 85 testes JVM passando com 100% de sucesso.
- [x] Validação dos testes instrumentados de UI (API 24 e 36) no CI.
- [x] Compilação limpa da variante `release` (`:app:assembleRelease`).
- [ ] Geração do APK release assinado via assistente do Android Studio para sideload no aparelho.
