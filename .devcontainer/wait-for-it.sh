#!/usr/bin/env bash
# Uso: wait-for-it host:port -- <comando>
set -e

if [[ "$1" == "" || "$1" == "--help" || "$1" == "-h" ]]; then
  echo "Uso: $0 host:port -- comando"
  exit 1
fi

HOSTPORT="$1"
shift
if [[ "$1" != "--" ]]; then
  echo "Erro: faltou separador --"
  exit 1
fi
shift

HOST="${HOSTPORT%:*}"
PORT="${HOSTPORT##*:}"

echo "Aguardando ${HOST}:${PORT}..."
until nc -z "$HOST" "$PORT" >/dev/null 2>&1; do
  sleep 1
done
echo "${HOST}:${PORT} disponível. Executando comando..."

exec "$@"
