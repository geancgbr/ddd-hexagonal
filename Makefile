.PHONY: test format coverage up down logs demo load stress privileges sbom deps licenses sonar dast

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
