#!/bin/sh
set -eu
cd "$(dirname "$0")" || exit 1

if [ -z "${DB_URL:-}" ]; then
  printf 'URL JDBC Oracle [jdbc:oracle:thin:@//oracle.fiap.com.br:1521/ORCL]: '
  IFS= read -r DB_URL
  DB_URL=${DB_URL:-jdbc:oracle:thin:@//oracle.fiap.com.br:1521/ORCL}
  export DB_URL
fi

if [ -z "${DB_USER:-}" ]; then
  printf 'Usuário/schema FIAP: '
  IFS= read -r DB_USER
  export DB_USER
fi

if [ -z "${DB_PASSWORD:-}" ]; then
  printf 'Senha do Oracle (não será exibida nem salva): '
  trap 'stty echo 2>/dev/null || true' EXIT HUP INT TERM
  stty -echo
  IFS= read -r DB_PASSWORD
  stty echo
  trap - EXIT HUP INT TERM
  printf '\n'
  export DB_PASSWORD
fi

sh scripts/iniciar-web.sh
