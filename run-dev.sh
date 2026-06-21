#!/usr/bin/env bash
# Sobe tudo para desenvolvimento local: Postgres + backend (Spring) + frontend (Next).
# Portas (sobrescrevíveis por env): DB 5433 | BACKEND 8080 | FRONT 3001
# Uso:  ./run-dev.sh        Para parar: ./stop-dev.sh
set -e
ROOT="$(cd "$(dirname "$0")" && pwd)"
DB_PORT="${DB_PORT:-5433}"
BACKEND_PORT="${BACKEND_PORT:-8080}"
FRONT_PORT="${FRONT_PORT:-3001}"

porta_ocupada() { ss -ltn 2>/dev/null | grep -q ":$1 "; }

# Espera um log conter "$2"; aborta se aparecer erro ou estourar o timeout ($3 s).
esperar_log() {
  local arquivo="$1" alvo="$2" limite="${3:-90}" i=0
  while ! grep -q "$alvo" "$arquivo" 2>/dev/null; do
    if grep -qiE "APPLICATION FAILED TO START|BindException|Address already in use|Endereço já em uso" "$arquivo" 2>/dev/null; then
      echo "  ✗ falhou ao subir. Últimas linhas:"; tail -15 "$arquivo"; exit 1
    fi
    i=$((i+1)); [ "$i" -ge "$limite" ] && { echo "  ✗ timeout. Veja $arquivo"; tail -15 "$arquivo"; exit 1; }
    sleep 1
  done
}

echo "▶ 1/3 Postgres (porta $DB_PORT)..."
docker rm -f lp_dev_db >/dev/null 2>&1 || true
# Volume nomeado persiste os dados entre restarts (conta, eventos, config MP).
docker run -d --name lp_dev_db \
  -e POSTGRES_DB=lista_presentes -e POSTGRES_USER=lista -e POSTGRES_PASSWORD=lista \
  -v lp_dev_pgdata:/var/lib/postgresql/data \
  -p "${DB_PORT}:5432" postgres:16 >/dev/null
until docker exec lp_dev_db pg_isready -U lista -d lista_presentes >/dev/null 2>&1; do sleep 1; done
echo "  ✓ banco pronto"

echo "▶ 2/3 Backend (porta $BACKEND_PORT)..."
# Mata instância anterior do backend (evita 'porta já em uso').
pkill -9 -f 'nathan-0.0.1-SNAPSHOT.jar' 2>/dev/null || true
sleep 1
if porta_ocupada "$BACKEND_PORT"; then
  echo "  ✗ porta $BACKEND_PORT já está em uso por OUTRO processo. Libere-a ou rode: BACKEND_PORT=8081 ./run-dev.sh"; exit 1
fi
export SPRING_DATASOURCE_URL="jdbc:postgresql://localhost:${DB_PORT}/lista_presentes"
export SPRING_DATASOURCE_USERNAME=lista
export SPRING_DATASOURCE_PASSWORD=lista
# Chaves de DEV (NÃO usar em produção). PII precisa ter 32 bytes (base64).
export PII_ENCRYPTION_KEY="$(printf '%s' 'dev-pii-key-32-bytes-exactly-XYZ' | base64)"
export PHONE_HMAC_KEY=dev-hmac-key
export JWT_SECRET=dev-jwt-secret-bem-grande-123456
# PUBLIC_URL (ex.: URL do ngrok) habilita o teste no celular + Mercado Pago:
# vira a 1ª origem do CORS (= back_url do MP) e a base do webhook (notification_url).
PUBLIC_URL="${PUBLIC_URL:-}"
if [ -n "$PUBLIC_URL" ]; then
  export CORS_ALLOWED_ORIGINS="${PUBLIC_URL},http://localhost:3000,http://localhost:${FRONT_PORT}"
  export MP_WEBHOOK_BASE_URL="$PUBLIC_URL"
  echo "  • URL pública (ngrok): $PUBLIC_URL  → CORS/back_url/webhook configurados"
else
  export CORS_ALLOWED_ORIGINS="http://localhost:3000,http://localhost:${FRONT_PORT}"
  export MP_WEBHOOK_BASE_URL=
fi
export SERVER_PORT="$BACKEND_PORT"
( cd "$ROOT" && ./gradlew -q bootJar )
nohup java -jar "$ROOT"/build/libs/nathan-0.0.1-SNAPSHOT.jar > /tmp/lp_backend.log 2>&1 &
echo $! > /tmp/lp_backend.pid
esperar_log /tmp/lp_backend.log "Started NathanApplication" 90
echo "  ✓ backend no ar (log: /tmp/lp_backend.log)"

echo "▶ 3/3 Frontend (porta $FRONT_PORT)..."
# O dev server vivo é "next-server" (o "next dev" é só o lançador) — matar os dois
# evita 'EADDRINUSE: porta 3001 já em uso' de uma execução anterior.
pkill -9 -f "next dev" 2>/dev/null || true
pkill -9 -f "next-server" 2>/dev/null || true
sleep 2
( cd "$ROOT/frontend" && [ -d node_modules ] || npm install )
nohup bash -c "cd '$ROOT/frontend' && npm run dev -- -p $FRONT_PORT" > /tmp/lp_frontend.log 2>&1 &
echo $! > /tmp/lp_frontend.pid
esperar_log /tmp/lp_frontend.log "Ready" 120
echo "  ✓ frontend no ar (log: /tmp/lp_frontend.log)"

echo ""
echo "Tudo no ar:"
echo "  • App (casal):    http://localhost:${FRONT_PORT}/admin/register"
echo "  • API:            http://localhost:${BACKEND_PORT}"
echo "  • Página pública: http://localhost:${FRONT_PORT}/e/<slug-do-evento>"
echo "Parar tudo:  ./stop-dev.sh"
