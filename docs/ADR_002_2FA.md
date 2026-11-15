# ADR 002 — Verificação em duas etapas (2FA) multicliente

Status: **Aprovado** (abordagem TOTP + códigos de recuperação). Implementação backend + Android pendente.
Data: 2026-11-14
Decisores: Renato (mantenedor). Relacionado: [`DECISOES.md`](./DECISOES.md), Gate E do plano de convergência.

## 1. Contexto

- Contas são compartilhadas entre web e Android; a sessão móvel usa Bearer opaco revogável (contrato de autenticação móvel / ADR 001).
- Hoje a conta é protegida apenas por senha → risco de tomada de conta (credential stuffing, phishing, vazamento de senha).
- O 2FA deve ser uma **política única da conta**, válida na web e no Android, sem duplicar regra de negócio no cliente.
- Para a demo da Feira Tecnológica o 2FA ainda não estará implementado; o risco é aceito temporariamente (ver DECISOES.md), com implementação planejada.

## 2. Decisão

Adotar **TOTP (RFC 6238)** como segundo fator, com **códigos de recuperação**:

- **Ativação** exige reautenticação (senha/sessão atual) e confirmação do primeiro código TOTP.
- **Segredo TOTP** gerado no servidor, exibido uma única vez (QR/uri `otpauth://`), armazenado cifrado; nunca em logs.
- **Códigos de recuperação** de uso único, armazenados apenas como hash; exibidos uma única vez na ativação.
- **Verificação** no login quando o usuário tem 2FA ativo; desafio com TTL curto e rate limiting.
- **Dispositivo confiável** opcional, com expiração e revogação.
- **Auditoria** de ativação/desativação/verificações e **revogação de sessões** em eventos de segurança.
- Android guarda o estado de sessão no Keystore; o segredo TOTP do usuário **não** é persistido no aparelho (pertence à conta, no servidor).

## 3. Threat model (resumo)

### Ativos
- Contas de usuário e dados pessoais; sessões (web e móveis); segredo TOTP; códigos de recuperação; segredos de sessão.

### Ameaças e mitigações
| Ameaça | Mitigação |
|---|---|
| Credential stuffing / senha vazada | Segundo fator TOTP obrigatório após ativação |
| Phishing da senha | TOTP reduz uso da senha isolada; passkeys consideradas no futuro |
| Brute force do código TOTP | Rate limiting + lockout por conta/IP; janela TOTP curta |
| Replay de código | Janela de validade e marcação de uso; desafio de TTL curto |
| Roubo de códigos de recuperação | Hash-only no servidor; uso único; regeneração invalida anteriores |
| Vazamento de segredo TOTP | Cifrado em repouso; exibição única; nunca em logs/telemetria |
| Sequestro de sessão | Revogação server-side; reautenticação para ações sensíveis; dispositivo confiável com expiração |
| MITM | TLS obrigatório fora de ambiente local; sem token em URL |
| Dispositivo comprometido | Keystore para sessão; 2FA não elimina risco residual (ver abaixo) |

### Riscos residuais
- **Phishing de TOTP em tempo real** (relay): mitigação futura com passkeys/WebAuthn.
- **Dispositivo com malware/root**: Keystore ajuda, mas não garante; aceitar e documentar.
- **Engenharia social para desativar 2FA**: exigir reautenticação forte + auditoria + notificação.

## 4. Alternativas consideradas

- **Passkeys (WebAuthn)**: resistente a phishing e melhor UX; maior complexidade multicliente e de backend. **Adiada** como evolução futura.
- **Códigos por e-mail**: mais simples, porém depende de e-mail autenticado (Brevo) e é menos seguro que TOTP. **Preterida**.
- **Sem 2FA**: risco de tomada de conta apenas com senha. **Rejeitada** como política final; aceita apenas temporariamente para a demo da feira.

## 5. Consequências

- **Backend (`LensClick`)**: rotas de enrollment/verify/recovery/disable; armazenamento cifrado do segredo; códigos de recuperação hash-only; desafio de login 2FA; rate limiting; auditoria; revogação de sessões.
- **Android**: telas de ativação (QR/segredo + códigos de recuperação), verificação no login e gerenciamento; integração ao fluxo de sessão existente; testes JVM/instrumentados.
- **Contrato móvel**: estender o contrato de autenticação com o desafio 2FA (ex.: resposta `202`/`2FA_REQUIRED` + endpoint de verificação), com testes de contrato multicliente.
- **Web**: parity da política de 2FA (mesma conta, mesmas regras).

## 6. Pendências

- Implementação backend + Android e testes de contrato.
- Aprovação formal do threat model completo e da política de recuperação.
- Definição de códigos de erro semânticos estáveis para o desafio 2FA.
- Reavaliar passkeys como evolução pós-feira.