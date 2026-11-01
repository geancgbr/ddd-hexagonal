.PHONY: test format up down logs demo

test:
	./mvnw -B verify

format:
	./mvnw -B spotless:apply

up:
	docker compose up -d --build --wait

down:
	docker compose down -v

logs:
	docker compose logs -f app

demo:
	./scripts/demo.sh
