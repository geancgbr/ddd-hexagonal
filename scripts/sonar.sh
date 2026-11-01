#!/usr/bin/env bash
set -euo pipefail

SONAR_URL="${SONAR_URL:-http://localhost:9000}"

docker compose --profile sonar up -d sonarqube

printf 'Aguardando o SonarQube subir'
until curl -fs "$SONAR_URL/api/system/status" | grep -q '"status":"UP"'; do
    printf '.'
    sleep 5
done
echo

if [[ -z "${SONAR_TOKEN:-}" ]]; then
    SONAR_TOKEN=$(curl -fs -u admin:admin -X POST "$SONAR_URL/api/user_tokens/generate" \
        -d "name=ddd-hexagonal-$(date +%s)" -d "type=GLOBAL_ANALYSIS_TOKEN" |
        python3 -c "import json, sys; print(json.load(sys.stdin)['token'])") || {
        echo "Não foi possível gerar o token com admin/admin. Gere um em $SONAR_URL e rode: SONAR_TOKEN=... make sonar"
        exit 1
    }
fi

./mvnw -B -q verify sonar:sonar \
    -Dsonar.host.url="$SONAR_URL" \
    -Dsonar.token="$SONAR_TOKEN" \
    -Dsonar.projectKey=ddd-hexagonal \
    -Dsonar.projectName=ddd-hexagonal \
    -Dsonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml

echo "Relatório: $SONAR_URL/dashboard?id=ddd-hexagonal (login admin/admin)"
