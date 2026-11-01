.PHONY: test format coverage up down logs demo load stress

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
