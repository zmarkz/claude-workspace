# Workspace: ~/Documents/claude/

## READ THIS FIRST — Every Time

You are working in a multi-project workspace. This directory contains multiple independent applications, a shared infrastructure platform, and project templates. Before doing ANY work, read this entire file to understand the workspace layout, routing rules, and conventions.

---

## Project Registry

Each project has a `project.json` at its root. To discover all projects dynamically:

```bash
for f in ~/Documents/claude/*/project.json; do echo "---"; cat "$f"; done
```

Use the `keywords` field in each `project.json` to match user requests to the right project. Below is the current registry (keep this updated when creating or removing projects):

| Project | Path | Type | Stack | Database | Port(s) | Status |
|---------|------|------|-------|----------|---------|--------|
| Portfolio Tracker API | `./portfolio-tracker/` | Spring Boot backend | Java 23, Spring Boot 3.2, Maven, JPA/Hibernate | MySQL 8 (`portfolio_tracker`) | 8080 | Active |
| Portfolio Tracker Frontend | `./portfolio-tracker-frontend/` | React SPA | React 19, TypeScript, Vite, TailwindCSS, Zustand | — | 5173 | Active |
| MCP Farm | `./mcp-farm/` | Infrastructure platform | Node.js, TypeScript, Fastify, MCP SDK, Drizzle ORM | PostgreSQL 16 (`mcp_farm`) | 9080, 8081, 8082 | Planned |
| Platform (Orchestration) | `./platform/` | Docker/Deploy/Scripts | Docker Compose, Nginx, Shell scripts | — | — | Active |

**IMPORTANT**: When you create a new project, you MUST update this table. When you remove a project, remove its row.

---

## Routing Rules

When the user asks about something, determine which project it belongs to and work WITHIN that project's directory. Use these rules:

### Keyword-Based Routing
- **portfolio, stocks, holdings, trading, investments, kite, zerodha** → `./portfolio-tracker/` or `./portfolio-tracker-frontend/`
  - If about API, backend, database, entities, services → `./portfolio-tracker/`
  - If about UI, dashboard, components, pages, charts → `./portfolio-tracker-frontend/`
- **MCP, gateway, agent farm, tool routing, MCP server** → `./mcp-farm/`
- **docker, deploy, compose, nginx, AWS, infrastructure** → `./platform/`
- **template, scaffold, new app** → See "New App Creation" below

### Ambiguous Requests
If a request could apply to multiple projects (e.g., "fix the login bug"), check:
1. What was the most recent project discussed in this conversation?
2. Ask the user if still ambiguous.

### Cross-Project Work
Some tasks span projects (e.g., "add a new API endpoint and update the frontend"). Work on each project sequentially:
1. Backend changes first
2. Frontend changes second
3. Platform/Docker changes last
4. Test the integration end-to-end

---

## Per-Project CLAUDE.md — Convention

Every project MUST have its own `CLAUDE.md` at its root. When working inside a project directory, read BOTH this root file AND the project's own `CLAUDE.md`.

### What goes in a per-project CLAUDE.md

Every project's CLAUDE.md MUST contain these sections (use this as a template):

```markdown
# {Project Name}

## Overview
One paragraph describing what this project does, who uses it, and its key purpose.

## Tech Stack
- Language/Framework: {e.g., Java 23 / Spring Boot 3.2}
- Build Tool: {e.g., Maven, npm, pnpm}
- Database: {e.g., MySQL 8, PostgreSQL 16}
- Key Libraries: {list the important ones}

## How to Run Locally
Step-by-step commands to start this project in development mode.
Include both standalone (without Docker) and Docker-based instructions.

## Project Structure
Key directories and files with brief explanations.
Focus on where a developer would look to make changes.

## Database
- Database name and connection pattern
- Key tables/entities and their relationships
- How to run migrations or seed data

## API Endpoints (if backend)
List the main endpoint groups with HTTP methods and paths.
Link to Swagger/OpenAPI docs if available.

## Environment Variables
Table of all required and optional environment variables.
| Variable | Required | Default | Description |
|----------|----------|---------|-------------|

## Key Patterns & Conventions
- How errors are handled
- How authentication works
- Naming conventions for files/classes/routes
- Testing approach

## Integration Points
How this project connects to other projects in the workspace.
- What APIs does it call?
- What APIs does it expose?
- What databases does it share (if any)?

## Common Tasks
Quick reference for frequent operations:
- How to add a new API endpoint
- How to add a new page/component
- How to run tests
- How to build for production
```

### Rules for maintaining per-project CLAUDE.md
- When you add new endpoints, update the CLAUDE.md
- When you add new environment variables, update the CLAUDE.md
- When you change the database schema, update the CLAUDE.md
- When you add significant new features, update the Overview
- Keep it accurate — an outdated CLAUDE.md is worse than none

---

## project.json Convention

Every project MUST have a `project.json` at its root for auto-discovery:

```json
{
  "name": "project-name",
  "displayName": "Human Readable Name",
  "type": "backend | frontend | fullstack | infrastructure | platform",
  "status": "active | planned | archived",
  "stack": ["java", "spring-boot", "mysql"],
  "port": 8080,
  "database": {
    "type": "mysql | postgresql | none",
    "name": "database_name"
  },
  "docker": {
    "service": "docker-compose-service-name",
    "dockerfile": "relative/path/to/Dockerfile"
  },
  "keywords": ["portfolio", "stocks", "trading"],
  "dependencies": ["other-project-names-this-depends-on"],
  "repository": "git-remote-url-if-separate-repo"
}
```

---

## New App Creation — Complete Procedure

When asked to create a new application, follow ALL of these steps in order. Do NOT skip any step.

### Step 1: Understand Requirements
Before writing any code, clarify:
- What does this app do? (purpose)
- Backend, frontend, or fullstack?
- What tech stack? (default: Java/Spring Boot for backends, React/Vite/TypeScript for frontends, Node/TypeScript for microservices/tools)
- What database? (default: MySQL for business apps, PostgreSQL for infrastructure)
- Does it need MCP gateway access?
- Does it need to integrate with existing apps?

### Step 2: Scaffold the Project
```bash
mkdir ~/Documents/claude/{app-name}
cd ~/Documents/claude/{app-name}
git init
```

Choose the appropriate template from `~/Documents/claude/project-templates/`:
- `spring-boot-api/` — Java backend with Spring Boot, JPA, security
- `node-api/` — Node.js/TypeScript backend with Fastify, Drizzle ORM
- `react-frontend/` — React SPA with TypeScript, Vite, TailwindCSS

Copy the template contents and adapt:
- Rename packages/modules to match the new app
- Update `pom.xml` or `package.json` with the correct name and dependencies
- Update `application.yml` or config files with correct database name and ports
- Remove any template-specific placeholder code

### Step 3: Create project.json
Create `~/Documents/claude/{app-name}/project.json` following the convention above.

### Step 4: Create per-project CLAUDE.md
Create `~/Documents/claude/{app-name}/CLAUDE.md` following the template in the "Per-Project CLAUDE.md" section above. Fill in ALL sections — do not leave placeholders.

### Step 5: Create Dockerfile
Create a production-ready Dockerfile in the project root. Follow the patterns from existing projects:
- Multi-stage builds (build stage + runtime stage)
- Non-root user
- Health check
- Proper .dockerignore

### Step 6: Wire into Platform
Update `~/Documents/claude/platform/docker-compose.yml`:
- Add the new service
- Set `DATABASE_URL` or equivalent env vars pointing to the shared database
- Set correct `depends_on` with health checks
- Expose the correct port

Update `~/Documents/claude/platform/scripts/init-databases.sql` (MySQL) or `init-databases-pg.sql` (PostgreSQL):
- Add `CREATE DATABASE IF NOT EXISTS {app_database};`

If the app needs MCP gateway access, also:
- Register it as a consumer in the MCP gateway seed script
- Create subscriptions for the MCPs it needs

### Step 7: Update Root CLAUDE.md
Update the **Project Registry table** in THIS file with the new project's details.

### Step 8: Initial Commit
```bash
cd ~/Documents/claude/{app-name}
git add .
git commit -m "feat: initial scaffold for {app-name}"
```

### Step 9: Build and Verify
```bash
cd ~/Documents/claude/platform
docker compose build {service-name}
docker compose up {service-name}
# Verify it starts, connects to DB, and responds on its port
```

### Step 10: Self-Check
Before reporting to the user that the new app is ready, verify:
- [ ] `project.json` exists and is complete
- [ ] `CLAUDE.md` exists and all sections are filled
- [ ] `Dockerfile` exists and builds successfully
- [ ] Service is in `platform/docker-compose.yml`
- [ ] Database is in `platform/scripts/init-databases.sql` or `init-databases-pg.sql`
- [ ] Root `CLAUDE.md` project registry table is updated
- [ ] Git repo is initialized with initial commit
- [ ] App starts and responds to health check

---

## Database Strategy

### Local Development
All databases run in Docker via `platform/docker-compose.yml`:
- **MySQL 8** — for business applications (portfolio-tracker, future apps)
- **PostgreSQL 16** — for infrastructure (MCP farm, agent farm)

Connection patterns from Docker network:
- MySQL: `mysql://dev:devpass@mysql:3306/{database_name}`
- PostgreSQL: `postgres://dev:devpass@postgres:5432/{database_name}`

Connection patterns from host (for IDE/tools):
- MySQL: `mysql://dev:devpass@localhost:3306/{database_name}`
- PostgreSQL: `postgres://dev:devpass@localhost:5432/{database_name}`

### Production (AWS)
- MySQL → Amazon RDS MySQL (db.t3.micro, free tier eligible)
- PostgreSQL → Amazon RDS PostgreSQL (db.t3.micro, free tier eligible)
- Connection strings provided via environment variables — same code, different config

### Adding a New Database
1. Add `CREATE DATABASE IF NOT EXISTS {name};` to `platform/scripts/init-databases.sql` (MySQL) or `platform/scripts/init-databases-pg.sql` (PostgreSQL)
2. Add the connection env var to the service in `docker-compose.yml`
3. Document it in the project's `CLAUDE.md`

---

## Docker & Container Management

### Starting Services
```bash
cd ~/Documents/claude/platform

# Start everything
docker compose up -d

# Start only infrastructure (databases, redis)
docker compose up -d mysql postgres redis

# Start a specific app and its dependencies
docker compose up -d portfolio-tracker

# Start MCP farm
docker compose up -d mcp-gateway agent-farm mcp-web-search
```

### Common Operations
```bash
# View running containers
docker compose ps

# View logs (follow)
docker compose logs -f {service}

# Rebuild a single service after code changes
docker compose build {service} && docker compose up -d {service}

# Shell into a container
docker compose exec {service} sh

# Database shell
docker compose exec mysql mysql -u dev -pdevpass
docker compose exec postgres psql -U dev

# Stop everything
docker compose down

# Stop and remove all data (nuclear option)
docker compose down -v
```

### Port Map (Local Development)
| Port | Service |
|------|---------|
| 3306 | MySQL |
| 5432 | PostgreSQL |
| 6379 | Redis |
| 8080 | Portfolio Tracker API |
| 5173 | Portfolio Tracker Frontend |
| 9080 | MCP Gateway (REST API, mapped from internal 8080) |
| 8081 | MCP Gateway (MCP Protocol) |
| 8082 | Agent Farm |

**Note on port conflicts**: Portfolio Tracker API and MCP Gateway both use 8080 internally. In docker-compose, the MCP Gateway is mapped to host port 9080. Inside the Docker network, services communicate by service name, so there's no conflict.

---

## AWS Deployment

### Architecture (Single EC2 + RDS)
- **EC2** (t3.small): Runs all Docker containers via docker-compose
- **RDS MySQL** (db.t3.micro): Business app databases
- **RDS PostgreSQL** (db.t3.micro): Infrastructure databases (MCP farm)
- **S3**: Static frontend assets, database backups

### Deploy Workflow
```bash
cd ~/Documents/claude/platform
./scripts/deploy.sh
```

### Environment Separation
- Local: `platform/.env.local` (gitignored)
- Production: `platform/.env.prod` on EC2 (never committed)
- Template: `platform/.env.prod.example` (committed, no secrets)

---

## Git Conventions

### Repository Structure
- Each project is its own git repo with its own `.git/`
- The parent `~/Documents/claude/` will transition away from being a single git repo
- Each new project gets `git init` at creation
- `platform/` is its own git repo

### Commit Messages
Use conventional commits:
- `feat:` — new feature
- `fix:` — bug fix
- `chore:` — maintenance, deps, config
- `docs:` — documentation only
- `refactor:` — code change that neither fixes a bug nor adds a feature
- `test:` — adding or updating tests

---

## Claude Code Workflow

### Working on a Specific Project
When the user's request maps to a specific project:
1. Read this root CLAUDE.md (you're doing that now)
2. Navigate to the project directory
3. Read the project's own CLAUDE.md
4. Read `project.json` for metadata
5. Do the work within that project's directory
6. Update the project's CLAUDE.md if you changed anything significant

### Working Across Projects
When the task spans multiple projects:
1. Plan the changes for each project first
2. Execute backend changes first, then frontend, then platform
3. Test each layer before moving to the next
4. Update all affected CLAUDE.md files

### Monitoring (Claude Remote)
When monitoring the system remotely:
```bash
cd ~/Documents/claude/platform
docker compose ps                          # check container health
docker compose logs --tail=50 {service}    # recent logs
```

---

## Template Usage Guidelines

Templates live in `~/Documents/claude/project-templates/`. They are NOT git repos — they are scaffolding that gets copied into new project directories.

### Available Templates

| Template | Path | Use When |
|----------|------|----------|
| Spring Boot API | `./project-templates/spring-boot-api/` | Java backend with REST API, JPA, security |
| Node.js API | `./project-templates/node-api/` | TypeScript microservice with Fastify |
| React Frontend | `./project-templates/react-frontend/` | React SPA with TypeScript, Vite, TailwindCSS |

### After Copying a Template
1. Search and replace all placeholder strings (`{{APP_NAME}}`, `{{APP_GROUP}}`, etc.)
2. Update `pom.xml` or `package.json` metadata
3. Remove any placeholder routes/components you don't need
4. Add app-specific dependencies
5. Configure the correct database and ports

---

## Important Reminders

1. **Never hardcode secrets.** Always use environment variables.
2. **Always create Dockerfiles** for new projects, even if running locally without Docker initially.
3. **Always update documentation** — root CLAUDE.md, project CLAUDE.md, project.json.
4. **Test in Docker** before considering a feature done. The Docker environment is the source of truth.
5. **Keep port mappings updated** in the Port Map table above when adding new services.
6. **Separate repos** — when creating a new project, always `git init` it as its own repo.
