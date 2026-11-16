# Wishlist: lista de presentes com importação por IA

[![CI](https://github.com/NathanLisandro/Wishlist/actions/workflows/ci.yml/badge.svg)](https://github.com/NathanLisandro/Wishlist/actions/workflows/ci.yml)

SaaS multi-tenant em que casais criam um evento (casamento, chá de panela, casa nova), publicam a lista de presentes por link, e os convidados confirmam presença, escolhem um presente ou presenteiam com dinheiro via Mercado Pago, sem precisar criar conta.

**Stack:** Java 25 · Spring Boot 4 · PostgreSQL · Flyway · Next.js 16 · React 19 · TypeScript · API da Anthropic (SDK Java) · Mercado Pago · Docker · Testcontainers

## Importação de lista com IA

O casal envia a lista do jeito que ela já existe: foto ou print, PDF, texto colado ou link de uma loja. O Claude extrai os itens e o casal revisa tudo num preview editável antes de gravar.

- **Entrada multimodal:** imagem e PDF vão direto para o modelo (visão e PDF nativos da API), sem OCR intermediário. Links passam por um buscador HTML antes da extração.
- **Saída estruturada:** os itens voltam num schema tipado (nome, variante, quantidade, link), derivado do record Java pelo SDK. Não há parsing de texto livre.
- **Estimativa de preço:** um segundo adapter estima o preço de cada item, usado para ordenar e sugerir valores.
- **IA atrás de um port:** `ExtratorListaIaPort` e `EstimadorPrecoIaPort` isolam o fornecedor. A feature liga e desliga por configuração (`AI_ENABLED`), e o modelo é definido por `ANTHROPIC_MODEL`.

Código: [`infrastructure/ai`](src/main/java/com/wishlist/nathan/infrastructure/ai) e [`application/usecase/ExtrairListaPresentesIa.java`](src/main/java/com/wishlist/nathan/application/usecase/ExtrairListaPresentesIa.java).

## Arquitetura

Clean Architecture com dependências apontando para dentro:

```
domain/          entidades e value objects puros, sem framework
application/     casos de uso + ports (interfaces)
infrastructure/  adapters: JPA, Mercado Pago, Anthropic, criptografia, HTTP
presentation/    controllers REST, erros RFC 7807
```

Entidades JPA são separadas do domínio e convertidas por mappers. Toda integração externa entra por um port.

## Segurança e confiabilidade

- **Dados pessoais protegidos:** nome e telefone do convidado são criptografados com AES-256-GCM. A busca por telefone usa HMAC (`phone_hash`), e nenhum dado pessoal vai para o log.
- **Sessões stateless** com JWT para o casal e para o convidado, e rate limiting (Bucket4j) no login e na sessão do convidado.
- **Reserva de presente idempotente e sem oversell:** lock de linha, `Idempotency-Key` e índice único parcial. Teste com 8 cliques simultâneos num item de 1 unidade resulta em exatamente 1 reserva.
- **Webhook do Mercado Pago idempotente:** deduplicação por id do evento. O pagamento só é confirmado pelo webhook, nunca pelo redirect do cliente.
- **Multi-tenant:** todo acesso é escopado por conta e evento.

## Outras funcionalidades

- Importação de presentes e convidados por CSV/XLSX, com relatório de erro por linha.
- Painel do casal com presenças, presentes reservados, dinheiro recebido e acessos recentes.
- Co-gestão do evento por link de convite, tema e fundo personalizáveis e modo surpresa.
- Página pública com SSR e Open Graph, para o preview no WhatsApp.

## Como rodar

```bash
cp .env.example .env   # preencha as chaves (openssl rand -base64 32)
./run-dev.sh           # Postgres + backend (8080) + frontend (3001)
./stop-dev.sh
```

Para a importação por IA, defina `ANTHROPIC_API_KEY` no `.env`. Testes do backend: `./gradlew build` (Testcontainers, requer Docker).

## Documentação

- [Resumo do projeto e endpoints](RESUMO.md)
- [Design (SDD)](docs/SDD-lista-presentes.md)
- [Integração com Mercado Pago](docs/MERCADO-PAGO.md)
- [Épico de personalização, IA e modo surpresa](docs/FEATURE-personalizacao-ia-surpresa.md)
