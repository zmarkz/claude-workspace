# Platform Orchestration - Project Guide

## Overview

This is the central orchestration layer for the platform ecosystem. It manages Docker Compose configurations, deployment automation, and nginx routing for all microservices.

**Stack**: Docker, Docker Compose, Nginx, Bash, Makefile

## Services

### Infrastructure
- **MySQL 8.0**: portfolio_tracker database on port 3306
- **PostgreSQL 16**: mcp_farm database on port 5432
- **Redis 7**: caching layer on port 6379

### Applications
- **portfolio-tracker**: Backend API on port 8080
- **portfolio-tracker-frontend**: React/Vue SPA on port 5173
- **mcp-gateway**: MCP API gateway on port 9080
- **agent-farm**: Agent orchestration service on port 8082
- **mcp-web-search**: Web search MCP server on port 3001

### Proxy
- **nginx**: Reverse proxy and load balancer (prod only, port 80/443)

## Port Map

| Service | Port | Purpose |
|---------|------|---------|
| portfolio-tracker | 8080 | Backend API |
| portfolio-tracker-frontend | 5173 | Frontend SPA |
| mcp-gateway | 9080 | MCP Gateway |
| agent-farm | 8082 | Agent Farm |
| mysql | 3306 | MySQL database |
| postgres | 5432 | PostgreSQL database |
| redis | 6379 | Redis cache |
| nginx | 80, 443 | Production proxy |

## Quick Start (Local Dev)

```bash
# Start all services
make up

# Start only infrastructure
make up-infra

# View logs
make logs

# Access services
# Frontend:        http://localhost:5173
# Backend API:     http://localhost:8080
# MCP Gateway:     http://localhost:9080
# Agent Farm:      http://localhost:8082
```

## Configuration

### Local Development
1. Copy `.env.local.example` to `.env.local`
2. Fill in API keys (ANTHROPIC_API_KEY, OPENAI_API_KEY, etc.)
3. Run `make up`

### Production
1. Copy `.env.prod.example` to `.env.prod`
2. Configure AWS RDS endpoints, EC2 host, domain, and API keys
3. Run `make deploy`

## Database Access

```bash
# MySQL CLI
make db-mysql

# PostgreSQL CLI
make db-postgres
```

## Adding New Services

To add a new service to the orchestration:

1. Add service definition to `docker-compose.yml`:
```yaml
  my-service:
    build: ../my-service
    ports:
      - "8090:8080"
    environment:
      DB_HOST: postgres
      DB_USER: dev
    depends_on:
      postgres:
        condition: service_healthy
    networks:
      - platform
```

2. If it needs a database, update `scripts/init-databases.sql` or `init-databases-pg.sql`

3. Add nginx routing in `nginx/nginx.conf`:
```nginx
location /my-service-api/ {
    proxy_pass http://my-service:8080/;
    proxy_set_header Host $host;
    proxy_set_header X-Real-IP $remote_addr;
    proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    proxy_set_header X-Forwarded-Proto $scheme;
}
```

4. Add service override in `docker-compose.prod.yml` if needed

## Deployment Workflow

1. **Local Testing**:
   ```bash
   make build-all
   make up
   # Test all services
   ```

2. **Backup Production**:
   ```bash
   make backup --upload-s3
   ```

3. **Deploy to EC2**:
   ```bash
   make deploy
   ```
   Deploys to EC2 host, pulls latest code, rebuilds and restarts services.

4. **Verify**:
   ```bash
   ssh -i .pem ubuntu@EC2_HOST
   docker compose -f docker-compose.prod.yml ps
   ```

## Backup & Recovery

```bash
# Backup databases locally
make backup

# Upload to S3
AWS_S3_BUCKET=my-bucket make backup --upload-s3

# Backups are timestamped: backups/mysql_backup_YYYYMMDD_HHMMSS.sql
```

## Health Checks

All services include health checks:
- MySQL: `mysqladmin ping`
- PostgreSQL: `pg_isready`
- Dependent services wait for database health before starting

## Useful Commands

```bash
make help          # Show all targets
make ps            # List running containers
make logs          # Follow service logs
make down          # Stop all services
make db-mysql      # Enter MySQL shell
make db-postgres   # Enter PostgreSQL shell
```

## Files Reference

- `docker-compose.yml` - Local development configuration
- `docker-compose.prod.yml` - Production overrides
- `nginx/nginx.conf` - Reverse proxy configuration
- `scripts/deploy.sh` - EC2 deployment automation
- `scripts/backup-db.sh` - Database backup utility
- `scripts/init-databases.sql` - MySQL initialization
- `scripts/init-databases-pg.sql` - PostgreSQL initialization
- `.env.local.example` - Local env vars template
- `.env.prod.example` - Production env vars template
- `Makefile` - Convenience commands
