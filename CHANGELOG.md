# Changelog — sessão de 21/06/2026

Resumo das mudanças feitas nesta sessão (redesign, novas features, correções,
deploy no Railway e observabilidade).

---

## 🎨 Redesign completo (visual + UX)

- Identidade nova em todo o app, inspirada no mockup do casal: **Instrument Serif + Inter**,
  paleta **plum / rosé / lilás**, cards de vidro, blobs desfocados, ícones que mudam de cor no hover.
- **Convidado** (`/e/[slug]`): landing com **foto do casal** (hero), citação, barra de data/local,
  e fluxo de presentes repensado — botão **Adicionar** → barra **Confirmar** → modal de confirmação →
  **tela de agradecimento**. Passo a passo sempre visível com ícones (Presença → Presentes → Pronto).
- **Admin** (`/admin`): login, registro e lista de eventos redesenhados + **dashboard em abas**:
  - **Visão geral** — métricas + feed "ao vivo" (polling) de quem está acessando.
  - **Presentes** — CRUD + importação CSV.
  - **Convidados** — lista/RSVP + importação CSV.
  - **Personalizar** — upload de foto + textos do evento + **preview por iframe**.
  - **Pagamento** — configuração do Mercado Pago.

## ⚙️ Funcionalidades novas

- **Foto do casal**: upload, storage local abstraído por port (`ArmazenamentoPort`), servida em `/media`.
- **Personalização do evento**: local, recado/citação, frase de despedida e **mensagem de agradecimento**
  (migrations `V3`, `V4`, `V5`).
- **CRUD de presentes** (admin): adicionar, editar, remover, **excluir todos**.
- **CRUD de convidados** (admin): editar nome/telefone/acompanhantes, remover, **excluir todos**,
  telefone exibido na lista.
- **Convidado pode escolher vários presentes** e trocar; **RSVP com confirmação única** + alternância
  (não-vou ↔ confirmar).
- **Telefone**: prefixo `+55` fixo, máscara automática `(67) 99843-7332`, normalização no backend
  (aceita só DDD + número, com ou sem máscara).
- **Dinheiro**: máscara pt-BR `R$ 1.000,00`; rótulo "Presentear com dinheiro".

## 🐛 Correções

- **CSV com BOM** (Excel/Google Sheets): o cabeçalho `nome` virava `﻿nome` e toda linha falhava
  na importação — corrigido no parser.
- **Sessão de convidado removido/recriado**: passou a retornar **404 "sessão expirou"** (re-login) em vez
  de estourar **500** ao reservar presente / presentear com dinheiro.
- **Webhook com corpo vazio** (IPN do Mercado Pago): gravar `""` numa coluna `jsonb` quebrava o insert
  (500 + loop de retry) — corrigido (só grava JSON válido; tolera `mp_event_id` nulo).
- **Mercado Pago não confirmava**: causa diagnosticada = o `PII_ENCRYPTION_KEY` foi trocado **depois** de
  salvar as credenciais, então o Access Token cifrado não descriptografava. Solução: **re-salvar** as
  credenciais com a chave atual e **nunca mais trocar** a chave com dados no banco.

## 🚀 Deploy no Railway

- Repositório no GitHub; **2 serviços** (backend via `Dockerfile` na raiz + frontend Nixpacks com
  Root Directory `frontend`) + **PostgreSQL** + **volume** para as fotos.
- **Proxy do Next** (`next.config.ts`): o navegador fala com **um domínio só** (o front), que faz proxy
  de `/auth`, `/account`, `/events`, `/public`, `/webhooks`, `/media` para o backend — sem CORS no browser.
- Variáveis por serviço documentadas; banco via `${{Postgres.PG*}}`; webhook/`back_url` via
  `MP_WEBHOOK_BASE_URL` / `CORS_ALLOWED_ORIGINS`.
- Scripts: `run-ngrok.sh` / `stop-ngrok.sh` (teste no celular com 1 túnel) e ajustes no `run-dev.sh`
  (mata `next-server`, honra `PUBLIC_URL`).
- Conexão ao banco de produção pelo **DBeaver** (via `DATABASE_PUBLIC_URL`).

## 🔍 Observabilidade

- Logs do **Mercado Pago** passam a mostrar o motivo real do erro (status + corpo da resposta).
- **Webhook** loga cada etapa (recebido, dedup, sem config, ignorado, consultado, atualizado).
- **`TratadorDeErros`**: catch-all 500 com uma `ref` curta para correlação nos logs.
- **Aspecto AOP `LogCasosDeUso`**: loga **todos** os casos de uso (ação, OK/FALHOU, duração, argumentos).
  Mostra **nome/telefone** (decisão do dono — logs só de operação), mas **mascara senha/token/secret**
  e não despeja bytes de upload. O login do convidado loga `nome + telefone + convidadoId` para amarrar
  as ações seguintes (que só carregam o id).

## 📦 Commits (sem co-author)

| Hash | Descrição |
|---|---|
| `8bf1e79` | fluxo Adicionar → Confirmar → agradecimento + passo a passo |
| `132ad3b` | logs de erro do MP + catch-all 500 |
| `999495d` | sessão com convidado removido falha limpa (re-login) |
| `4c2dd13` | logs no fluxo do webhook |
| `b2cdacf` | fix do `jsonb` no webhook (corpo vazio) |
| `d3663b4` | máscara de moeda pt-BR |
| `6136b42` | excluir todos os presentes |
| `6134db1` | aspecto que loga todos os casos de uso |

> Verificação: `./gradlew test` (Testcontainers) e `npm run build` verdes.
