# Resumo do projeto — Plataforma de Lista de Presentes

SaaS multi-tenant onde casais criam eventos (casamento, chá de panela, casa nova…),
publicam uma **lista de presentes** por link, e os convidados se identificam pelo
**telefone** (sem cadastro) para **confirmar presença**, **escolher presentes** ou
**dar dinheiro** (Mercado Pago).

> Documentos de referência: [docs/SDD-lista-presentes.md](docs/SDD-lista-presentes.md) (design),
> [docs/CLAUDE.md](docs/CLAUDE.md) (guia), [docs/MERCADO-PAGO.md](docs/MERCADO-PAGO.md) (pagamentos).

---

## Stack

- **Backend:** Java 25 + Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation, Flyway), PostgreSQL. Build com **Gradle**.
- **Frontend:** Next.js 16 (App Router) + React 19 + TypeScript + Tailwind v4 + shadcn/ui (em `frontend/`).
- **Pagamentos:** Mercado Pago (Checkout Pro + Webhooks).
- **Arquitetura:** Clean Architecture (domínio puro → application/ports → infrastructure/adapters → presentation).

---

## Como rodar (desenvolvimento)

```bash
./run-dev.sh     # sobe Postgres (5433) + backend (8080) + frontend (3001)
./stop-dev.sh    # para tudo
```
- App do casal: http://localhost:3001/admin/register
- Página pública do evento: http://localhost:3001/e/&lt;slug&gt;
- API: http://localhost:8080

> Portas alternativas (5433/3001) porque 5432 e 3000 já são usadas por outros
> projetos na máquina. O Postgres usa volume `lp_dev_pgdata` (dados persistem).

Comandos avulsos: `./gradlew build` (backend + testes), `cd frontend && npm run build`.

---

## O que foi implementado

### Iteração 1 — Fundação
- `build.gradle` com todas as dependências; `application.yml` com perfis local/prod; logging estruturado (JSON/ECS em prod).
- Migrations Flyway: `V1__init.sql` (schema 3NF completo) + `V2__guest_access_log.sql` (rastreamento de acessos).
- `docker-compose.yml` (Postgres + Dozzle), estrutura de pacotes e **camada de domínio** (value objects, enums, entidades puras, sem framework).

### Iteração 2 — Backend núcleo
- **Segurança:** criptografia de PII (AES-256-GCM), HMAC do telefone (`phone_hash`), JWT (sessão admin e convidado), Spring Security stateless + filtro JWT, erros padronizados RFC 7807.
- **Persistência JPA:** entidades `*Jpa` separadas do domínio + repositórios Spring Data + adapters (mappers), enums do Postgres via `@JdbcTypeCode(NAMED_ENUM)`.
- **Casos de uso:** registrar/autenticar admin, criar evento, adicionar presente/convidado, sessão do convidado por telefone, listar presentes, **escolher presente (idempotente, sem oversell)**, cancelar, confirmar/cancelar presença.
- Teste de concorrência (Testcontainers): 8 cliques simultâneos em item de 1 unidade → **exatamente 1 reserva**.

### Iteração 3 — Backend restante
- **Mercado Pago:** configurar credenciais (cifradas), criar presente em dinheiro (preference), **webhook idempotente** (dedup por `mp_event_id`, status por `mp_payment_id`).
- **Importação CSV/XLSX:** parser (Commons CSV + Apache POI), importar presentes e convidados com relatório de erros por linha (`import_job`), templates para download. Idempotente (re-subir não duplica).
- **Dashboard do admin:** presenças, presentes reservados, dinheiro recebido e acessos recentes (nome decifrado só no painel).
- **Rate limiting** (Bucket4j) no login e na sessão do convidado (429).

### Iteração 4 — Frontend (Next.js)
- **Convidado** (`/e/[slug]`): landing com **SSR + Open Graph** (preview no WhatsApp); login por telefone; **wizard em 2 passos** → 1) confirmar presença (com acompanhantes), 2) escolher presente, com **dinheiro como card fixo** e **confirmação "tem certeza?"** antes de presentear.
- **Admin** (`/admin`): login/registro, lista/criar eventos, gestão do evento (dashboard + importar CSV + configurar Mercado Pago com URL de webhook pronta para copiar).
- Identidade visual "rosé/wedding", mobile-first, botões com cursor de clique; tratamento de sessão expirada (volta ao login automaticamente).

---

## Regras de negócio / segurança (inegociáveis)
- **PII protegida:** nome e telefone do convidado **criptografados**; busca por telefone só via **HMAC** (`phone_hash`); nada de PII em log.
- **Idempotência:** reserva de presente (lock de linha + `Idempotency-Key` + índice único parcial) e webhook do MP (dedup).
- **Multi-tenant:** todo acesso escopado por conta/evento.
- **3NF:** quantidade reservada e nº de acompanhantes são **calculados** (COUNT), não armazenados.
- **Pagamento confirmado só pelo webhook** (server-side), nunca pelo redirect do cliente.
- **Segredos só em env vars** (`.env` no `.gitignore`, `.env.example` versionado).

---

## Principais endpoints

| Área | Endpoint |
|---|---|
| Admin auth | `POST /auth/register`, `POST /auth/login` |
| Conta | `GET /account/me`, `GET/PUT /account/payment-config` |
| Eventos (admin) | `GET/POST /events`, `POST /events/{id}/gifts|guests`, `.../import`, `.../template`, `GET /events/{id}/dashboard` |
| Público (convidado) | `GET /public/events/{slug}`, `POST .../guest-session`, `GET .../gifts`, `POST/DELETE .../gifts/{id}/claim`, `POST .../money-gift`, `POST/DELETE .../rsvp` |
| Webhook | `POST /webhooks/mercadopago?accountId=...` |

---

## Testes
- **Backend:** `./gradlew build` (10 testes Testcontainers — migrations, idempotência/concorrência da reserva, fluxo MP com stub, importação, dashboard, rate limit).
- **Frontend:** `npm run build` (typecheck + build).

---

## O que falta / próximos passos
- Teste real de pagamento ponta a ponta (ngrok + credenciais de teste do MP — ver [docs/MERCADO-PAGO.md](docs/MERCADO-PAGO.md)).
- Pós-MVP: verificação de e-mail (OTP), personalização de tema/foto por evento, notificações ao casal, Row-Level Security, expurgo LGPD, deploy no Railway.
