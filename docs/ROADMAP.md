# Roadmap do LensClick App

> Evolução do aplicativo Android conectado à plataforma LensClick.

## Estado Atual

Aplicativo Android em Kotlin e Jetpack Compose com suporte a autenticação por Bearer no Android Keystore, descoberta pública, perfil profissional, portfólio com upload, orçamentos, propostas, agenda, favoritos, conversas, avaliações, exportação/exclusão LGPD e estrutura de 2FA TOTP (ADR 002).

## Fases do Projeto

- **Fase 1 — Fundação e CI:** Concluída (Gradle, SDK 36, JDK 21, testes JVM e instrumentados).
- **Fase 2 — Integração Core:** Concluída (Autenticação, descoberta, orçamentos, propostas, agenda).
- **Fase 3 — Funcionalidades Avançadas:** Implementadas no cliente (2FA, conversas, avaliações, LGPD); validação e2e dependente de deploy/contratos no backend (`Renato1909/LensClick`).
- **Fase 4 — Feira Tecnológica (Sideload):** Geração de APK release assinado localmente com Keystore fora do repositório para demonstração.
