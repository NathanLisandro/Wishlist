# CLAUDE.md — Plataforma de Lista de Presentes

Guia operacional para o agente (Claude Code) trabalhar neste repositório.
**Sempre consulte `docs/SDD.md` para o design completo** — este arquivo é o resumo.

## O que é

SaaS multi-tenant de lista de presentes para casamento, chá de panela, chá de casa nova etc. Casais (admins) criam eventos, importam lista de presentes e de convidados; convidados se identificam pelo telefone (sem cadastro), escolhem presentes, podem dar dinheiro (Mercado Pago) e confirmam presença.

## Stack

- **Backend:** Java 25 + Spring Boot 4.1 (Web MVC, Data JPA, Security, Validation), Flyway, PostgreSQL. Build com **Gradle**, pacote `com.wishlist.nathan`.
  > Nota Boot 4: as autoconfigurações foram modularizadas — a integração do Flyway exige o módulo `org.springframework.boot:spring-boot-flyway` (não basta `flyway-core`).
- **Frontend:** Next.js (React) + Tailwind.
- **Pagamentos:** Mercado Pago (Checkout Pro + Webhooks).
- **Deploy:** Railway (serviços separados backend/frontend + Postgres). Config 100% por env vars.

## Arquitetura — Clean Architecture

Dependências apontam sempre para dentro (DIP). O domínio **não** conhece Spring/JPA.

```
com.<org>.giftregistry        # renomeie <org> para o seu pacote
├── domain/                    # entidades + value objects + regras puras (sem framework)
├── application/
│   ├── usecase/               # casos de uso (ver SDD §3.3)
│   ├── port/                  # interfaces in/out (repositórios, gateways)
│   └── dto/                   # DTOs de entrada/saída
├── infrastructure/
│   ├── persistence/           # @Entity JPA + repositórios (adapters) + mappers domínio<->JPA
│   ├── payment/               # adapter Mercado Pago
│   ├── file/                  # parsers CSV/XLSX (Commons CSV / Apache POI)
│   ├── security/              # cripto de PII, HMAC do telefone, auth
│   └── config/
└── presentation/              # controllers REST + presenters + request/response models
```

**Regra:** entidades de domínio são POJOs puros; a persistência usa **models JPA separados** em `infrastructure/persistence`, com mappers. Não anote o domínio com `@Entity`.

## Regras inegociáveis

1. **Segurança / PII:** nome e telefone do convidado **criptografados** (`*_encrypted bytea`); lookup de telefone só por **HMAC** (`phone_hash`), nunca pelo número em claro. Token do Mercado Pago criptografado. Nada de PII em log.
2. **Idempotência:** `EscolherPresente` = lock de linha no `gift_item` + header `Idempotency-Key` único + unique parcial `(gift_item_id, guest_id) WHERE status='CLAIMED'`. Webhook do MP deduplica por `mp_event_id` e `mp_payment_id`.
3. **Multi-tenant:** toda query escopada por `account_id`/`event_id`. Nunca retornar dado de outro tenant ou de outro convidado.
4. **3NF:** não armazenar dado derivado — quantidade reservada e nº de acompanhantes são **calculados** (COUNT), não colunas.
5. **Soft-delete:** item esgotado vira `SOLD_OUT` (some da lista pública), nunca é apagado.
6. **Pagamento:** confirmação **só** pelo webhook server-side. Nunca confiar no redirect do cliente.
7. **Segredos:** só em env vars / Railway Variables. `.env` no `.gitignore`; manter `.env.example`. Nunca commitar chave de cripto nem token do MP.

## Convenções

- Validação com Bean Validation nos DTOs de entrada; encoding de saída (evitar XSS em nome de presente / recado).
- Migrations versionadas em `src/main/resources/db/migration` (`V1__init.sql`, `V2__...`). **Nunca editar uma migration já aplicada** — crie a próxima.
- Erros → resposta consistente (sugestão: RFC 7807 / `application/problem+json`).
- Testes: unitário no domínio/casos de uso; integração com **Testcontainers** (Postgres) para repositórios e para o fluxo de claim/idempotência (cliques concorrentes).

## Comandos (Gradle)

```bash
docker compose up -d        # sobe Postgres + Dozzle (logs em http://localhost:9999)
./gradlew bootRun           # sobe a app (Flyway aplica as migrations no boot)
./gradlew test              # testes (Testcontainers exige Docker)
./gradlew build             # build do jar
```

## Ordem de implementação

1. ✅ **Fundação**: deps, `application.yml`, Flyway (`V1`+`V2`), Docker Compose, logging estruturado, Clean Architecture e domínio.
2. ✅ **Backend núcleo**: cripto/HMAC/JWT, Spring Security stateless, persistência JPA + mappers, casos de uso de admin essencial e do fluxo do convidado (incl. reserva idempotente sem oversell), RFC 7807.
3. ✅ **Backend restante**: Mercado Pago (presente em dinheiro + webhook idempotente), importação CSV/XLSX (POI/Commons CSV, com `import_job`/relatório), dashboard do admin, rate limiting (Bucket4j).
4. ⏭️ **Frontend Next.js** consumindo a API.

> Pendências pós-MVP: verificação de e-mail, RLS, expurgo LGPD, notificações ao casal. A integração real do Mercado Pago precisa de credenciais sandbox (`TEST-...`) configuradas via `PUT /account/payment-config`.

## Variáveis de ambiente (ver `.env.example`)

```
DATABASE_URL=
JWT_SECRET=
PII_ENCRYPTION_KEY=        # cripto de nome/telefone
PHONE_HMAC_KEY=            # HMAC para phone_hash (lookup)
MP_WEBHOOK_BASE_URL=
CORS_ALLOWED_ORIGINS=
```

## Fluxo de trabalho

Antes de implementar uma feature grande: **planeje primeiro** (plan mode), confirme contra o `docs/SDD.md`, e só então gere o código. Trabalhe feature por feature para manter o contexto enxuto.


## SEMPRE SIGA O SOLID! METODOS SEPARADOS COM NOMES RELEVANTES E QUE REALMENTE DESCREVE O QUE O METODO FAZ. NÃO CRIE COMPLEXIDADE CICLOMATICA E DEIXE O CÓDIGO MANUTENIVEL.

## TESTES UNITARIOS SÃO EXTREMAMENTES NECESSARIOS
## TESTES END TO END TAMBÉM SÃO NECESSARIOS!