# Mercado Pago — passo a passo (local e produção)

Como configurar o presente em dinheiro com confirmação por webhook. A confirmação do
pagamento **só acontece pelo webhook** (server-side), nunca pelo redirect do cliente.

---

## 0. Pré-requisitos (uma vez)

1. Acesse o painel: https://www.mercadopago.com.br/developers/panel/app e crie uma
   **aplicação** (tipo *Checkout Pro*).
2. Em **Credenciais de teste**, copie o **Access Token** e a **Public Key**
   (hoje começam com `APP_USR-`, geradas dentro da aba de teste — são de sandbox).
3. (Para pagar de mentira) Crie **usuários de teste** em *Contas de teste*: um
   **vendedor** e um **comprador**. Use o token do vendedor na app e pague logado
   como comprador, com os [cartões de teste](https://www.mercadopago.com.br/developers/pt/docs/checkout-pro/additional-content/your-integrations/test/cards)
   (nome `APRO` = aprovado).

---

## 1. Local (com ngrok)

O Mercado Pago não enxerga `localhost` — exponha o backend (porta 8080) com um túnel.

```bash
# 1) suba tudo (banco + backend + front)
./run-dev.sh

# 2) em outro terminal, exponha o backend
ngrok http 8080         # copie a URL https://xxxx.ngrok-free.app
```

> Para o webhook funcionar nas cobranças, **suba o backend já sabendo a URL pública**:
> ```bash
> ./stop-dev.sh
> MP_WEBHOOK_BASE_URL=https://xxxx.ngrok-free.app ./run-dev.sh
> ```

3. No app (**http://localhost:3001 → Área do casal → Gerenciar evento → aba Pagamento**):
   - Cole **Access Token** e **Public Key** de teste.
   - O app monta a **URL do webhook** automaticamente (com o `accountId`). Preencha
     "Seu domínio público" com a URL do ngrok e clique **Copiar**.
4. No painel do MP (**Suas integrações → sua aplicação → Webhooks**):
   - Cole a URL copiada, marque o evento **Pagamentos**, salve.
   - Copie a **Assinatura secreta** → cole no campo **Webhook Secret** do app e salve.
5. Teste: na página pública do evento, aba **Dar dinheiro** → paga como usuário de
   teste. O webhook chega, o status vira **APPROVED** e aparece no dashboard.

> Atalho para o 1º teste: deixe o **Webhook Secret em branco** — o backend pula a
> validação de assinatura (menos seguro, só para ver o fluxo). Depois preencha.

---

## 2. Produção (ex.: Railway)

1. Faça deploy do backend; ele terá um domínio público, ex.: `https://api.seudominio.com`.
2. Configure as variáveis de ambiente do serviço backend:
   ```
   MP_WEBHOOK_BASE_URL=https://api.seudominio.com
   CORS_ALLOWED_ORIGINS=https://app.seudominio.com   # domínio do frontend
   # + DATABASE/JWT/PII/HMAC já existentes
   ```
3. Use as **credenciais de produção** do Mercado Pago (não as de teste) no app
   (aba Pagamento) — Access Token + Public Key + Webhook Secret.
4. No painel do MP, cadastre a URL de webhook de produção (a que o app mostra, com o
   `accountId`), evento **Pagamentos**, e copie a assinatura secreta para o app.
5. Importante: as **back_urls** (retorno do pagamento) precisam ser um domínio real
   (não `localhost`) — em prod isso já acontece via `CORS_ALLOWED_ORIGINS`.

---

## Como funciona por dentro (resumo)

- `POST /public/events/{slug}/money-gift` cria o `money_gift` (PENDING) e uma
  *preference* no MP com `external_reference = money_gift.id` e
  `notification_url = MP_WEBHOOK_BASE_URL/webhooks/mercadopago?accountId=<conta>`.
- `POST /webhooks/mercadopago?accountId=...` valida a assinatura, **deduplica por
  `mp_event_id`**, consulta o pagamento na API do MP e atualiza o status por
  `mp_payment_id` (único). Reenvio é no-op.
