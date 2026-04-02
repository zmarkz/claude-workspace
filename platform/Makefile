.PHONY: up down ps logs up-infra up-portfolio up-mcp db-mysql db-postgres deploy backup build-all help

help:
	@echo "Platform Orchestration - Available targets:"
	@echo "  up              - Start all services (local dev)"
	@echo "  down            - Stop all services"
	@echo "  ps              - Show running containers"
	@echo "  logs            - View logs from all services"
	@echo "  up-infra        - Start only database services"
	@echo "  up-portfolio    - Start portfolio tracker services"
	@echo "  up-mcp          - Start MCP services"
	@echo "  db-mysql        - Open MySQL CLI"
	@echo "  db-postgres     - Open PostgreSQL CLI"
	@echo "  deploy          - Deploy to production"
	@echo "  backup          - Backup databases"
	@echo "  build-all       - Build all images"

up:
	docker compose up -d
	@echo "All services started. Waiting for health checks..."
	@sleep 5
	@docker compose ps

down:
	docker compose down

ps:
	docker compose ps

logs:
	docker compose logs -f

up-infra:
	docker compose up -d mysql postgres redis
	@echo "Infrastructure services started"

up-portfolio:
	docker compose up -d portfolio-tracker portfolio-tracker-frontend
	@echo "Portfolio tracker services started"

up-mcp:
	docker compose up -d mcp-gateway agent-farm mcp-web-search
	@echo "MCP services started"

db-mysql:
	docker compose exec mysql mysql -u dev -pdevpass -D portfolio_tracker

db-postgres:
	docker compose exec postgres psql -U dev -d mcp_farm

deploy:
	chmod +x scripts/deploy.sh
	./scripts/deploy.sh

backup:
	chmod +x scripts/backup-db.sh
	./scripts/backup-db.sh

build-all:
	docker compose build --no-cache
	@echo "All images built"
