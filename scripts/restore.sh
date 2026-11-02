#!/usr/bin/env bash
set -euo pipefail

file="${1:-$(ls -t backups/*.dump 2>/dev/null | head -1)}"
if [[ -z "$file" || ! -f "$file" ]]; then
    echo "Uso: make restore FILE=backups/orders-AAAAMMDD-HHMMSS.dump" >&2
    exit 1
fi

docker compose exec -T postgres pg_restore -U postgres -d orders --clean --if-exists --single-transaction < "$file"
echo "Banco restaurado a partir de $file"
