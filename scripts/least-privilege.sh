#!/usr/bin/env bash
set -uo pipefail

as_app() {
    docker compose exec -T -e PGPASSWORD=orders_app postgres \
        psql -h localhost -U orders_app -d orders -v ON_ERROR_STOP=1 -tAc "$1" 2>&1
}

attempt() {
    local description="$1" sql="$2" expected="$3"
    local output
    if output=$(as_app "$sql"); then
        result="permitido"
    else
        result="negado"
    fi
    local mark="✅"
    [[ "$result" == "$expected" ]] || mark="❌"
    printf '%s %-38s %-10s %s\n' "$mark" "$description" "$result" "$(echo "$output" | head -1)"
}

echo "Usuário da aplicação (orders_app):"
attempt "ler pedidos" "SELECT count(*) FROM orders" "permitido"
attempt "gravar produto" "INSERT INTO products VALUES (gen_random_uuid(), 'teste', 1, true)" "permitido"
attempt "apagar a tabela de pedidos" "DROP TABLE orders" "negado"
attempt "criar uma tabela" "CREATE TABLE invasor (id int)" "negado"
attempt "alterar a estrutura" "ALTER TABLE orders ADD COLUMN x int" "negado"
