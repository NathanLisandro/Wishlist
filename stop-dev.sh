#!/usr/bin/env bash
# Para o que o run-dev.sh subiu.
[ -f /tmp/lp_frontend.pid ] && kill "$(cat /tmp/lp_frontend.pid)" 2>/dev/null && rm -f /tmp/lp_frontend.pid
[ -f /tmp/lp_backend.pid ] && kill "$(cat /tmp/lp_backend.pid)" 2>/dev/null && rm -f /tmp/lp_backend.pid
# O processo do dev server do Next se chama "next-server" (não "next dev").
pkill -f 'next dev' 2>/dev/null || true
pkill -f 'next-server' 2>/dev/null || true
pkill -f 'nathan-0.0.1-SNAPSHOT.jar' 2>/dev/null || true
docker rm -f lp_dev_db >/dev/null 2>&1 || true
echo "Parado."
