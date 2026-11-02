#!/usr/bin/env bash
set -euo pipefail

mkdir -p backups
file="backups/orders-$(date +%Y%m%d-%H%M%S).dump"
docker compose exec -T postgres pg_dump -U postgres -d orders --format=custom > "$file"
echo "Backup gravado em $file"
