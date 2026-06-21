#!/usr/bin/env bash
# Sobe o app exposto por ngrok (1 túnel no frontend) JÁ configurado para testar no
# celular E para o Mercado Pago (webhook + back_url usam a URL pública).
#
# Fluxo: abre o túnel → descobre a URL → (re)sobe tudo com PUBLIC_URL=<url do ngrok>.
# Uso:  ./run-ngrok.sh        Parar o túnel: ./stop-ngrok.sh   Parar o app: ./stop-dev.sh
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
FRONT_PORT="${FRONT_PORT:-3001}"

if ! command -v ngrok >/dev/null 2>&1; then
  echo "  ✗ ngrok não encontrado. Instale e rode:  ngrok config add-authtoken <SEU_TOKEN>"; exit 1
fi

# Sobe um túnel novo no frontend (mesmo que o front ainda não esteja no ar — ele já já sobe).
pkill -f "ngrok http" 2>/dev/null || true
sleep 1
echo "▶ Abrindo túnel ngrok para a porta ${FRONT_PORT}..."
nohup ngrok http "${FRONT_PORT}" --log=stdout > /tmp/lp_ngrok.log 2>&1 &
echo $! > /tmp/lp_ngrok.pid

URL=""
for i in $(seq 1 30); do
  URL="$(curl -s http://127.0.0.1:4040/api/tunnels 2>/dev/null \
        | grep -o 'https://[a-zA-Z0-9.-]*\(ngrok-free\.app\|ngrok\.app\|ngrok\.io\|ngrok-free\.dev\)' \
        | head -1)"
  [ -n "$URL" ] && break
  if grep -qi "ERR_NGROK\|authentication failed\|account is limited" /tmp/lp_ngrok.log 2>/dev/null; then
    echo "  ✗ ngrok recusou. Veja /tmp/lp_ngrok.log"
    echo "    Provável: falta autenticar →  ngrok config add-authtoken <SEU_TOKEN>"; exit 1
  fi
  sleep 1
done
[ -z "$URL" ] && { echo "  ✗ Não consegui obter a URL do ngrok. Veja /tmp/lp_ngrok.log"; exit 1; }
echo "  ✓ Túnel: $URL"

echo "▶ (Re)subindo o app com a URL pública (CORS/back_url/webhook + Open Graph)..."
PUBLIC_URL="$URL" "$ROOT/run-dev.sh"

echo ""
echo "  ✓ Pronto! Abra no CELULAR:"
echo "      • App do casal:    ${URL}/admin/register   (ou /admin/login)"
echo "      • Página pública:  ${URL}/e/<slug-do-evento>"
echo ""
echo "  Mercado Pago: na aba Pagamento do evento, em \"Seu domínio público\" use:"
echo "      ${URL}"
echo "  Painel de requisições do ngrok:  http://localhost:4040"
echo "  Parar o túnel:  ./stop-ngrok.sh"
