#!/usr/bin/env bash
set -euo pipefail

MERMAID="minlag/mermaid-cli:12.0.1"
work="$(mktemp -d)"
trap 'rm -rf "$work"' EXIT
chmod 777 "$work"

status=0
while IFS= read -r file; do
    name="$(echo "$file" | tr '/' '_')"
    cp "$file" "$work/$name"
    if docker run --rm -u "$(id -u):$(id -g)" -v "$work":/data "$MERMAID" \
        -q -i "/data/$name" -o "/data/out-$name" > "$work/$name.log" 2>&1; then
        echo "✅ $file"
    else
        echo "❌ $file"
        cat "$work/$name.log"
        status=1
    fi
done < <(grep -rl '```mermaid' --include='*.md' README.md docs)

exit $status
