#!/usr/bin/env bash
set -euo pipefail

STRUCTURIZR="structurizr/structurizr:2026.10.26"
MERMAID="minlag/mermaid-cli:12.0.1"
docs="$PWD/docs/c4"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
chmod 777 "$work"

docker run --rm -v "$docs":/usr/local/structurizr:ro "$STRUCTURIZR" \
    validate -w /usr/local/structurizr/workspace.dsl
docker run --rm -v "$docs":/usr/local/structurizr:ro -v "$work":/out "$STRUCTURIZR" \
    export -w /usr/local/structurizr/workspace.dsl -f mermaid -o /out > /dev/null

for view in Contexto:contexto Conteineres:conteineres Componentes:componentes AplicarCupom:aplicar-cupom Implantacao:implantacao; do
    key="${view%%:*}"
    name="${view##*:}"
    docker run --rm -u "$(id -u):$(id -g)" -v "$work":/data "$MERMAID" \
        -q -i "/data/structurizr-$key.mmd" -o "/data/$name.png" -s 2
    cp "$work/$name.png" "$docs/$name.png"
    echo "docs/c4/$name.png"
done
