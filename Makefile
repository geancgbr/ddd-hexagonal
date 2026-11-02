STRUCTURIZR := structurizr/structurizr:2026.10.26

.PHONY: test format coverage up down logs demo load stress privileges sbom deps licenses sonar dast openapi c4 c4-local docs-check backup restore

test:
	./mvnw -B verify

format:
	./mvnw -B spotless:apply

coverage: test
	open target/site/jacoco/index.html

up:
	docker compose up -d --build --wait

down:
	docker compose down -v

logs:
	docker compose logs -f app

demo:
	./scripts/demo.sh

load:
	docker compose --profile load run --rm k6 run /scripts/carga.js

stress:
	docker compose --profile load run --rm k6 run /scripts/estresse.js

privileges:
	./scripts/least-privilege.sh

sbom:
	./mvnw -B -q cyclonedx:makeBom
	@echo "SBOM gerado em target/bom.json"

deps: sbom
	docker run --rm -v "$(CURDIR)":/src -v trivy-cache:/root/.cache aquasec/trivy:0.76.1 \
		sbom --quiet --scanners vuln --severity HIGH,CRITICAL --exit-code 1 /src/target/bom.json 2>/dev/null

licenses: sbom
	docker run --rm -v "$(CURDIR)":/src -v trivy-cache:/root/.cache aquasec/trivy:0.76.1 \
		sbom --quiet --scanners license /src/target/bom.json 2>/dev/null

sonar:
	./scripts/sonar.sh

dast:
	mkdir -p target/zap && chmod 777 target/zap
	docker compose --profile dast run --rm zap
	@echo "Relatório: target/zap/zap-relatorio.html"

openapi:
	./mvnw -B -q test -Dtest=OpenApiContractTest -Dsurefire.failIfNoSpecifiedTests=false -Dmaven.test.failure.ignore=true
	cp target/openapi.json docs/api/openapi.json
	@echo "Contrato atualizado em docs/api/openapi.json"

backup:
	./scripts/backup.sh

restore:
	./scripts/restore.sh $(FILE)

c4:
	./scripts/c4.sh

c4-local:
	@echo "Structurizr em http://localhost:8081 (Ctrl+C para sair)"
	docker run --rm -p 8081:8080 -v "$(CURDIR)/docs/c4":/usr/local/structurizr $(STRUCTURIZR) local

docs-check:
	./scripts/docs-check.sh

