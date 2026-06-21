#!/usr/bin/env bash
# Para o túnel ngrok aberto pelo run-ngrok.sh.
pkill -f "ngrok http" 2>/dev/null && echo "✓ túnel ngrok encerrado" || echo "nenhum túnel ngrok rodando"
rm -f /tmp/lp_ngrok.pid
