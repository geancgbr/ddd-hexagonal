#!/usr/bin/env bash
set -euo pipefail

API="${API:-http://localhost:8080}"

step() {
    printf '\n\033[1;34m▶ %s\033[0m\n' "$1"
}

call() {
    local method="$1" path="$2" body="${3:-}"
    if [[ -n "$body" ]]; then
        curl -s -X "$method" "$API$path" -H 'Content-Type: application/json' -d "$body"
    else
        curl -s -X "$method" "$API$path"
    fi
}

field() {
    python3 -c "import json, sys; print(json.load(sys.stdin)['$1'])"
}

step "Cadastrar produtos no catálogo"
keyboard=$(call POST /products '{"name": "Teclado mecânico", "price": 349.90}' | field id)
mouse=$(call POST /products '{"name": "Mouse sem fio", "price": 129.50}' | field id)
echo "teclado=$keyboard"
echo "mouse=$mouse"

step "Criar um pedido (rascunho)"
order=$(call POST /orders '{"customerId": "cliente-42"}' | field id)
echo "pedido=$order"

step "Adicionar itens (o mesmo produto duas vezes soma a quantidade)"
call POST "/orders/$order/items" "{\"productId\": \"$keyboard\", \"quantity\": 1}" > /dev/null
call POST "/orders/$order/items" "{\"productId\": \"$mouse\", \"quantity\": 2}" > /dev/null
call POST "/orders/$order/items" "{\"productId\": \"$keyboard\", \"quantity\": 1}" | python3 -m json.tool --no-ensure-ascii

step "Regra de negócio: produto inativo não entra no pedido (422)"
call POST "/products/$mouse/deactivate" > /dev/null
call POST "/orders/$order/items" "{\"productId\": \"$mouse\", \"quantity\": 1}" | python3 -m json.tool --no-ensure-ascii

step "Fechar o pedido"
call POST "/orders/$order/place" | python3 -m json.tool --no-ensure-ascii

step "Regra de negócio: pedido fechado não aceita itens (422)"
call POST "/orders/$order/items" "{\"productId\": \"$keyboard\", \"quantity\": 1}" | python3 -m json.tool --no-ensure-ascii

step "Eventos de domínio publicados (log da aplicação)"
docker compose logs app 2>/dev/null | grep "Evento de domínio" | sed 's/.*Evento de domínio: /  /' || true
