# Platform Orchestration

Central orchestration layer for the platform ecosystem. Manages Docker Compose configurations, deployment automation, and reverse proxy routing for all microservices.

## Quick Start

```bash
# Install dependencies
docker --version  # Ensure Docker is installed

# Setup environment
cp .env.local.example .env.local
# Edit .env.local with your API keys

# Start all services
make up

# View logs
make logs

# Stop services
make down
```

## Access Points

- **Frontend**: http://localhost:5173
- **Backend API**: http://localhost:8080/api
- **MCP Gateway**: http://localhost:9080
- **Agent Farm**: http://localhost:8082/agent-api

## Services

- MySQL 8.0 (port 3306)
- PostgreSQL 16 (port 5432)
- Redis 7 (port 6379)
- Portfolio Tracker backend (port 8080)
- Portfolio Tracker frontend (port 5173)
- MCP Gateway (port 9080)
- Agent Farm (port 8082)
- MCP Web Search (port 3001)

## Documentation

See [CLAUDE.md](CLAUDE.md) for detailed documentation on:
- Architecture overview
- Deployment workflow
- Adding new services
- Database management
- Production deployment

## Available Commands

```bash
make up              # Start all services
make down            # Stop all services
make ps              # Show running containers
make logs            # View service logs
make deploy          # Deploy to production
make backup          # Backup databases
make build-all       # Rebuild all images
```

## Project Structure

```
platform/
├── docker-compose.yml          # Local dev configuration
├── docker-compose.prod.yml     # Production overrides
├── nginx/
│   └── nginx.conf              # Reverse proxy config
├── scripts/
│   ├── deploy.sh               # EC2 deployment script
│   ├── backup-db.sh            # Database backup script
│   ├── init-databases.sql      # MySQL initialization
│   └── init-databases-pg.sql   # PostgreSQL initialization
├── .env.local.example          # Local env template
├── .env.prod.example           # Production env template
├── Makefile                    # Convenience commands
├── CLAUDE.md                   # Detailed documentation
└── README.md                   # This file
```

## License

Proprietary
