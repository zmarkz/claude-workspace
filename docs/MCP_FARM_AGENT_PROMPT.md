# MCP Farm Agent Build Prompt

## Your Mission

You are a senior software architect and full-stack engineer. Build an MCP Farm and Agent Farm platform. Follow the **THINK → PLAN → EXECUTE → VERIFY → MOVE ON** cycle for each phase.

---

## Workspace Context

- **Project Location**: `~/Documents/claude/mcp-farm/`
- **Initialize as**: New git repository
- **Parent Platform**: `~/Documents/claude/platform/` (shared orchestration layer)
  - PostgreSQL 16 on port 5432 via `docker-compose.yml`
  - Shared scripts in `scripts/` folder
  - Root `CLAUDE.md` with workspace conventions
- **After Scaffolding, You Must**:
  1. Create `project.json` in mcp-farm root
  2. Create `CLAUDE.md` in mcp-farm root
  3. Update root `CLAUDE.md` to reference mcp-farm
  4. Add `CREATE DATABASE mcp_farm;` to `platform/scripts/init-databases-pg.sql`
  5. Ensure `docker-compose.yml` postgres service includes `mcp_farm` in initialization

---

## Tech Stack (Non-Negotiable)

- **Language & Runtime**: TypeScript (strict mode), Node.js 20+
- **HTTP Framework**: Fastify (with TypeScript support)
- **MCP SDK**: `@modelcontextprotocol/sdk` (native HTTP transport)
- **AI/LLM Integration**: Vercel AI SDK (`ai` package) — supports Anthropic, OpenAI, Ollama
- **Database**: PostgreSQL 16 + `drizzle-orm` with migrations
- **Queue/Background Jobs** (Phase 7+): Redis 7 + BullMQ
- **Docker**: All services containerized, shared platform network
- **Package Manager**: pnpm workspaces (monorepo)
- **Testing**: Vitest
- **Environment**: Use `.env.local` for secrets, `.env.prod` for production

---

## Monorepo Structure

```
mcp-farm/
├── pnpm-workspace.yaml                # pnpm workspace config
├── package.json                       # Root metadata
├── tsconfig.json                      # Shared TS config
├── .gitignore
├── .env.example
├── CLAUDE.md                          # Project guide
├── project.json                       # pnpm-workspace metadata
│
├── packages/
│   ├── shared/                        # Shared code across all packages
│   │   ├── package.json
│   │   ├── src/
│   │   │   ├── db/
│   │   │   │   ├── schema.ts          # Drizzle schema
│   │   │   │   ├── migrations/        # Drizzle migrations
│   │   │   │   └── client.ts          # Database client factory
│   │   │   ├── types/
│   │   │   │   ├── mcp.ts             # MCP types
│   │   │   │   ├── agent.ts           # Agent types
│   │   │   │   └── api.ts             # Shared API types
│   │   │   ├── utils/
│   │   │   │   ├── logger.ts          # Structured logging
│   │   │   │   ├── errors.ts          # Custom error classes
│   │   │   │   └── validation.ts      # Input validation
│   │   │   └── index.ts               # Barrel exports
│   │   ├── tsconfig.json
│   │   └── vitest.config.ts
│   │
│   ├── gateway/                       # MCP Gateway (REST API + MCP Server)
│   │   ├── package.json
│   │   ├── src/
│   │   │   ├── index.ts               # Fastify entry point
│   │   │   ├── routes/
│   │   │   │   ├── registry.ts        # /api/mcps CRUD
│   │   │   │   ├── consumers.ts       # /api/consumers
│   │   │   │   ├── subscriptions.ts   # /api/subscriptions
│   │   │   │   └── tools.ts           # /api/tools/call
│   │   │   ├── mcp/
│   │   │   │   ├── client-pool.ts     # MCP client management
│   │   │   │   ├── server.ts          # Native MCP server (port 8081)
│   │   │   │   └── tool-router.ts     # Route tools by namespace
│   │   │   ├── backends/              # Mock/real backends for MCP servers
│   │   │   │   ├── web-search.ts
│   │   │   │   ├── aws.ts
│   │   │   │   └── atlassian.ts
│   │   │   └── middleware/
│   │   │       ├── auth.ts
│   │   │       └── error-handler.ts
│   │   ├── tsconfig.json
│   │   └── vitest.config.ts
│   │
│   ├── agent-farm/                    # Agent Orchestration Service
│   │   ├── package.json
│   │   ├── src/
│   │   │   ├── index.ts               # Fastify entry point
│   │   │   ├── routes/
│   │   │   │   ├── templates.ts       # Agent template CRUD
│   │   │   │   ├── tasks.ts           # Task CRUD & execution
│   │   │   │   └── health.ts          # Health checks
│   │   │   ├── llm/
│   │   │   │   ├── provider.ts        # LLM provider factory (Anthropic/OpenAI/Ollama)
│   │   │   │   └── tool-bridge.ts     # Convert MCP tools to AI SDK format
│   │   │   ├── workers/
│   │   │   │   ├── agent-worker.ts    # Ephemeral agent execution
│   │   │   │   └── task-executor.ts   # Sync/async task runner
│   │   │   ├── queue/
│   │   │   │   ├── bull-queue.ts      # BullMQ queue integration (Phase 7)
│   │   │   │   └── webhooks.ts        # Async task completion webhooks
│   │   │   └── middleware/
│   │   │       └── error-handler.ts
│   │   ├── tsconfig.json
│   │   └── vitest.config.ts
│   │
│   └── mcp-servers/                   # Custom MCP server implementations
│       ├── web-search/                # Brave API web search
│       │   ├── package.json
│       │   ├── src/
│       │   │   ├── index.ts           # MCP server entry
│       │   │   ├── tools/
│       │   │   │   └── search.ts      # Tool definitions
│       │   │   └── handlers/
│       │   │       └── search-handler.ts
│       │   └── tsconfig.json
│       │
│       ├── aws/                       # AWS SDK integration MCP
│       │   ├── package.json
│       │   ├── src/
│       │   │   ├── index.ts
│       │   │   ├── tools/
│       │   │   │   ├── ec2.ts
│       │   │   │   ├── s3.ts
│       │   │   │   └── lambda.ts
│       │   │   └── handlers/
│       │   └── tsconfig.json
│       │
│       └── atlassian/                 # Jira/Confluence MCP
│           ├── package.json
│           ├── src/
│           │   ├── index.ts
│           │   ├── tools/
│           │   │   ├── jira.ts
│           │   │   └── confluence.ts
│           │   └── handlers/
│           └── tsconfig.json
│
├── scripts/
│   ├── setup.sh                       # Initial setup (git, db, env)
│   ├── migrate.sh                     # Run Drizzle migrations
│   ├── seed.sh                        # Populate seed data
│   ├── e2e-test.sh                    # End-to-end test suite
│   └── docker-build.sh                # Build all containers
│
├── tests/
│   ├── e2e/
│   │   ├── gateway.test.ts            # REST API tests
│   │   ├── mcp-protocol.test.ts       # Native MCP server tests
│   │   └── agent-farm.test.ts         # Agent execution tests
│   └── integration/
│       └── full-workflow.test.ts
│
├── docker-compose.yml                 # Local dev services
├── Dockerfile                         # Multi-stage build (or per-package)
├── Makefile                           # Convenience commands
└── README.md                          # Project overview
```

---

## Database Schema (PostgreSQL 16)

Complete SQL for all tables with columns, constraints, and indexes:

```sql
-- mcp_servers: Registry of MCP server definitions
CREATE TABLE IF NOT EXISTS mcp_servers (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  mcp_version VARCHAR(50) NOT NULL DEFAULT '1.0',
  transport_type VARCHAR(50) NOT NULL DEFAULT 'http', -- 'http', 'stdio', 'sse'
  endpoint_url VARCHAR(2048),
  stdio_command VARCHAR(2048),
  environment_vars JSONB DEFAULT '{}',
  tool_count INTEGER DEFAULT 0,
  health_check_interval_seconds INTEGER DEFAULT 30,
  last_health_check_at TIMESTAMP,
  last_health_status VARCHAR(50), -- 'healthy', 'degraded', 'unhealthy'
  metadata JSONB DEFAULT '{}',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE INDEX idx_mcp_servers_name ON mcp_servers(name);
CREATE INDEX idx_mcp_servers_deleted_at ON mcp_servers(deleted_at);

-- consumers: Applications using the MCP Farm
CREATE TABLE IF NOT EXISTS consumers (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  api_key VARCHAR(255) NOT NULL UNIQUE,
  api_key_hash VARCHAR(255) NOT NULL,
  rate_limit_per_minute INTEGER DEFAULT 100,
  rate_limit_per_day INTEGER DEFAULT 10000,
  is_active BOOLEAN DEFAULT true,
  metadata JSONB DEFAULT '{}',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE INDEX idx_consumers_api_key ON consumers(api_key);
CREATE INDEX idx_consumers_is_active ON consumers(is_active);
CREATE INDEX idx_consumers_deleted_at ON consumers(deleted_at);

-- subscriptions: MCP server access for consumers
CREATE TABLE IF NOT EXISTS subscriptions (
  id SERIAL PRIMARY KEY,
  consumer_id INTEGER NOT NULL REFERENCES consumers(id) ON DELETE CASCADE,
  mcp_server_id INTEGER NOT NULL REFERENCES mcp_servers(id) ON DELETE CASCADE,
  is_active BOOLEAN DEFAULT true,
  can_read BOOLEAN DEFAULT true,
  can_write BOOLEAN DEFAULT false,
  can_execute BOOLEAN DEFAULT true,
  subscribed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_subscriptions_consumer_id ON subscriptions(consumer_id);
CREATE INDEX idx_subscriptions_mcp_server_id ON subscriptions(mcp_server_id);
CREATE INDEX idx_subscriptions_is_active ON subscriptions(is_active);
CREATE UNIQUE INDEX idx_subscriptions_unique ON subscriptions(consumer_id, mcp_server_id)
  WHERE deleted_at IS NULL;

-- tool_definitions: Cached tool definitions from MCP servers
CREATE TABLE IF NOT EXISTS tool_definitions (
  id SERIAL PRIMARY KEY,
  mcp_server_id INTEGER NOT NULL REFERENCES mcp_servers(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  input_schema JSONB NOT NULL,
  output_schema JSONB,
  category VARCHAR(100),
  requires_auth BOOLEAN DEFAULT false,
  is_deprecated BOOLEAN DEFAULT false,
  cached_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tool_definitions_mcp_server_id ON tool_definitions(mcp_server_id);
CREATE INDEX idx_tool_definitions_name ON tool_definitions(name);
CREATE UNIQUE INDEX idx_tool_definitions_unique ON tool_definitions(mcp_server_id, name);

-- tool_calls: Execution history of tool invocations
CREATE TABLE IF NOT EXISTS tool_calls (
  id SERIAL PRIMARY KEY,
  consumer_id INTEGER NOT NULL REFERENCES consumers(id) ON DELETE SET NULL,
  mcp_server_id INTEGER NOT NULL REFERENCES mcp_servers(id) ON DELETE SET NULL,
  tool_name VARCHAR(255) NOT NULL,
  input_params JSONB NOT NULL,
  output_result JSONB,
  error_message TEXT,
  execution_time_ms INTEGER,
  status VARCHAR(50) NOT NULL DEFAULT 'pending', -- 'pending', 'success', 'error', 'timeout'
  ip_address VARCHAR(45),
  user_agent VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  executed_at TIMESTAMP,
  metadata JSONB DEFAULT '{}'
);

CREATE INDEX idx_tool_calls_consumer_id ON tool_calls(consumer_id);
CREATE INDEX idx_tool_calls_mcp_server_id ON tool_calls(mcp_server_id);
CREATE INDEX idx_tool_calls_status ON tool_calls(status);
CREATE INDEX idx_tool_calls_created_at ON tool_calls(created_at);

-- agent_templates: Reusable agent configurations
CREATE TABLE IF NOT EXISTS agent_templates (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255) NOT NULL UNIQUE,
  description TEXT,
  llm_provider VARCHAR(50) NOT NULL, -- 'anthropic', 'openai', 'ollama'
  llm_model VARCHAR(255) NOT NULL,
  system_prompt TEXT NOT NULL,
  temperature DECIMAL(3, 2) DEFAULT 0.7,
  max_tokens INTEGER DEFAULT 4096,
  mcp_server_ids INTEGER[] DEFAULT '{}',
  tool_config JSONB DEFAULT '{}',
  metadata JSONB DEFAULT '{}',
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE INDEX idx_agent_templates_name ON agent_templates(name);
CREATE INDEX idx_agent_templates_deleted_at ON agent_templates(deleted_at);

-- agent_tasks: Task execution log for agents
CREATE TABLE IF NOT EXISTS agent_tasks (
  id SERIAL PRIMARY KEY,
  agent_template_id INTEGER NOT NULL REFERENCES agent_templates(id) ON DELETE SET NULL,
  consumer_id INTEGER NOT NULL REFERENCES consumers(id) ON DELETE CASCADE,
  task_type VARCHAR(50) NOT NULL, -- 'sync', 'async'
  input_prompt TEXT NOT NULL,
  system_override TEXT,
  execution_mode VARCHAR(50) DEFAULT 'sync', -- 'sync', 'async'
  status VARCHAR(50) NOT NULL DEFAULT 'pending', -- 'pending', 'running', 'success', 'error', 'timeout'
  result TEXT,
  error_message TEXT,
  tool_calls_count INTEGER DEFAULT 0,
  execution_time_ms INTEGER,
  webhook_url VARCHAR(2048),
  webhook_delivered_at TIMESTAMP,
  queue_job_id VARCHAR(255),
  started_at TIMESTAMP,
  completed_at TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  metadata JSONB DEFAULT '{}'
);

CREATE INDEX idx_agent_tasks_agent_template_id ON agent_tasks(agent_template_id);
CREATE INDEX idx_agent_tasks_consumer_id ON agent_tasks(consumer_id);
CREATE INDEX idx_agent_tasks_status ON agent_tasks(status);
CREATE INDEX idx_agent_tasks_execution_mode ON agent_tasks(execution_mode);
CREATE INDEX idx_agent_tasks_created_at ON agent_tasks(created_at);

-- audit_logs: Comprehensive audit trail
CREATE TABLE IF NOT EXISTS audit_logs (
  id SERIAL PRIMARY KEY,
  consumer_id INTEGER REFERENCES consumers(id) ON DELETE SET NULL,
  action VARCHAR(100) NOT NULL,
  resource_type VARCHAR(100),
  resource_id INTEGER,
  old_values JSONB,
  new_values JSONB,
  ip_address VARCHAR(45),
  user_agent VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_logs_consumer_id ON audit_logs(consumer_id);
CREATE INDEX idx_audit_logs_action ON audit_logs(action);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at);
```

---

## 9 Sequential Phases (Verify After Each)

### Phase 1: Foundation & Infrastructure

**Objective**: Set up monorepo, Docker, PostgreSQL, and shared types/utilities.

**THINK**:
- What's the minimal structure needed to support all future services?
- How do we share types and utilities across packages?
- How do we initialize the database in a reusable way?

**PLAN**:
1. Initialize git repo at `~/Documents/claude/mcp-farm/`
2. Create pnpm workspace config with root package.json
3. Create shared package with:
   - Drizzle schema and migrations
   - TypeScript types for MCP, Agent, API
   - Logger utility (structured JSON)
   - Custom error classes
4. Create Dockerfile multi-stage or per-package
5. Add docker-compose.yml for local dev
6. Create `.env.example`
7. Update platform's `init-databases-pg.sql` with `mcp_farm` database
8. Create `CLAUDE.md` project guide
9. Create `project.json` with workspace metadata

**EXECUTE**:
- Run `git init && git add .` in mcp-farm
- Install dependencies: `pnpm install`
- Create `.env.local` from `.env.example`
- Run migrations: `pnpm -F shared run db:migrate`
- Verify database exists: `psql postgres://dev:password@localhost:5432/mcp_farm`

**VERIFY**:
- [ ] Git repo initialized, files committed
- [ ] `pnpm list -r` shows all packages
- [ ] `docker compose up -d postgres` brings up database
- [ ] Migrations run without error
- [ ] `SELECT * FROM mcp_servers;` returns empty table
- [ ] Logger outputs structured JSON
- [ ] No TypeScript errors: `pnpm -r run type-check`

**MOVE ON** after all checks pass and commit.

---

### Phase 2: Gateway - Core REST API

**Objective**: Build Fastify REST API for MCP registry, consumers, subscriptions, and sync tool calls.

**THINK**:
- What REST endpoints do clients need to register MCPs and call tools?
- How do we validate and authenticate requests?
- How do we route tool calls by namespace (mcpName__toolName)?
- What do mock backends look like before real MCP connections?

**PLAN**:
1. Create gateway package with Fastify setup
2. Implement routes:
   - `POST /api/mcps` - Register MCP server
   - `GET /api/mcps` - List registered MCPs
   - `GET /api/mcps/:id` - Get MCP details
   - `PUT /api/mcps/:id` - Update MCP
   - `DELETE /api/mcps/:id` - Soft delete MCP
   - `POST /api/consumers` - Register consumer app
   - `GET /api/consumers` - List consumers
   - `POST /api/subscriptions` - Subscribe consumer to MCP
   - `GET /api/subscriptions` - List subscriptions
   - `POST /api/tools/call` - Execute tool (sync mode, mock backends)
   - `GET /health` - Health check
3. Create mock backends for web-search, aws, atlassian
4. Add request validation middleware
5. Add error handler middleware
6. Add CORS if needed

**EXECUTE**:
- Create `packages/gateway/src/index.ts` with Fastify
- Implement all routes with database operations
- Add mock tool execution handlers
- Create seed data script to populate test data
- Test endpoints with curl/Postman

**VERIFY**:
- [ ] `pnpm -F gateway run dev` starts on port 8080
- [ ] `POST /api/mcps` creates MCP entry
- [ ] `GET /api/mcps` returns list
- [ ] `POST /api/consumers` creates consumer
- [ ] `POST /api/subscriptions` links consumer to MCP
- [ ] `POST /api/tools/call` with mock data returns result
- [ ] Invalid requests return 400/422 with validation errors
- [ ] Soft deletes don't return deleted records
- [ ] No TypeScript errors, all tests pass

**MOVE ON** after verification and commit.

---

### Phase 3: MCP Client Pool & Real Connections

**Objective**: Implement real MCP client connections via HTTP transport, first real MCP server (web-search), and wire tool router to backends.

**THINK**:
- How do we manage persistent connections to multiple MCP servers?
- How do we handle health checks and reconnection?
- What does the HTTP transport look like?
- How do we parse mcpName__toolName and route to real backends?

**PLAN**:
1. Create `gateway/src/mcp/client-pool.ts`:
   - Maintain map of MCP server ID → MCP client
   - Lazy initialize on first use
   - Health check every 30s (update `last_health_check_at`)
   - Auto-reconnect on failure
   - Resource cleanup on shutdown
2. Implement web-search MCP server:
   - Create `packages/mcp-servers/web-search/` with standalone Fastify
   - Implement Brave Search API backend
   - Export tools: `search`, `get_url_content`
   - Serve on port 3001
3. Create `gateway/src/mcp/tool-router.ts`:
   - Parse `mcpName__toolName` format
   - Look up MCP server by name
   - Get tool from MCP client pool
   - Execute with parameters
   - Return result
4. Wire `/api/tools/call` to real MCP backends
5. Add error handling for timeouts, disconnections

**EXECUTE**:
- Implement client pool with connection state management
- Create web-search MCP with Brave API calls
- Update tool router to handle real connections
- Test with real HTTP calls
- Update health check endpoint to report MCP health

**VERIFY**:
- [ ] `pnpm -F web-search run dev` starts on port 3001
- [ ] Web-search MCP exports search tools
- [ ] `docker compose up` includes web-search service
- [ ] `POST /api/tools/call` with `mcp=web-search&tool=search` calls real MCP
- [ ] Health checks run every 30s, update `last_health_check_at`
- [ ] Failed connections trigger reconnect
- [ ] Tool execution timeout returns proper error
- [ ] All tests pass, no TODOs remain

**MOVE ON** after verification and commit.

---

### Phase 4: MCP Protocol Server (Native Interface)

**Objective**: Expose native MCP server interface on port 8081 so Claude Desktop and Cursor can connect directly.

**THINK**:
- What is the MCP server specification?
- How do we use Streamable HTTP transport in `@modelcontextprotocol/sdk`?
- How do we list tools and handle initialization from Claude?
- What's the difference between client and server mode?

**PLAN**:
1. Create `gateway/src/mcp/server.ts`:
   - Use `@modelcontextprotocol/sdk/server/index.js`
   - Implement MCP Server class
   - Handle `initialize` request (list capabilities)
   - Handle `resources/list` (none for now)
   - Handle `tools/list` (aggregate all subscribed MCP tools for connected consumer)
   - Handle `tools/call` (route through tool-router)
   - Use HTTP transport on port 8081
2. Create middleware to extract consumer from request headers or config
3. Register MCP server in gateway startup
4. Document how to connect Claude Desktop

**EXECUTE**:
- Implement MCP server in gateway
- Test connection from `mcp-inspector` or Claude Desktop
- Verify tools are discoverable
- Execute tool through native protocol

**VERIFY**:
- [ ] `pnpm -F gateway run dev` includes MCP server on port 8081
- [ ] `npx mcp-inspector` can connect
- [ ] `/tools/list` returns all available tools
- [ ] `/tools/call` executes tool and returns result
- [ ] Claude Desktop can add mcp-farm as MCP server
- [ ] Claude can call tools through the farm
- [ ] No TypeScript errors, tests pass

**MOVE ON** after verification and commit.

---

### Phase 5: Agent Farm Core Service

**Objective**: Build Agent Farm service with LLM provider abstraction, tool bridge, and agent execution.

**THINK**:
- How do we support multiple LLM providers (Anthropic, OpenAI, Ollama)?
- How do we convert MCP tools to Vercel AI SDK format?
- How do we execute agents synchronously vs asynchronously?
- What does an ephemeral agent worker do?

**PLAN**:
1. Create agent-farm package with Fastify
2. Implement LLM provider layer (`src/llm/provider.ts`):
   - Factory function to create LLM model based on provider name
   - Support Anthropic, OpenAI, Ollama
   - Load API keys from environment
3. Implement tool bridge (`src/llm/tool-bridge.ts`):
   - Accept MCP tool definitions
   - Convert to Vercel AI SDK `Tool` format
   - Wrap execution to call gateway `/api/tools/call`
4. Implement agent worker (`src/workers/agent-worker.ts`):
   - Accept template, input prompt, MCP tools
   - Create ephemeral LLM instance
   - Run agentic loop with tool calls
   - Return final result
5. Implement task manager routes:
   - `POST /api/agent-templates` - Create template
   - `GET /api/agent-templates` - List templates
   - `POST /api/tasks` - Create task (sync or async)
   - `GET /api/tasks/:id` - Get task status
   - `GET /health` - Health check
6. Create task executor for sync execution
7. Add webhook support for async task completion

**EXECUTE**:
- Implement LLM provider factory
- Create tool bridge connecting to gateway
- Implement agent worker with Vercel AI SDK
- Create REST endpoints for templates and tasks
- Test with real LLM API (Anthropic by default)

**VERIFY**:
- [ ] `pnpm -F agent-farm run dev` starts on port 8082
- [ ] `POST /api/agent-templates` creates template with system prompt
- [ ] `POST /api/tasks` executes agent task synchronously
- [ ] Agent calls tools via bridge, gets results
- [ ] Task result contains final output
- [ ] LLM provider switches between Anthropic/OpenAI based on env
- [ ] Error handling for invalid templates, timeout
- [ ] No TypeScript errors, tests pass

**MOVE ON** after verification and commit.

---

### Phase 6: Additional MCP Servers & Multi-MCP Agents

**Objective**: Build AWS and Atlassian MCPs, create agent templates combining multiple MCPs.

**THINK**:
- What tools should AWS and Atlassian MCPs expose?
- How do we handle authentication (API keys, OAuth)?
- How do we test integration without exposing real credentials?
- How do we design agent templates that orchestrate multiple MCPs?

**PLAN**:
1. Create AWS MCP (`packages/mcp-servers/aws/`):
   - Implement tools: ec2-list-instances, s3-list-buckets, lambda-list-functions
   - Use AWS SDK with environment-based credentials
   - Serve on port 3002
2. Create Atlassian MCP (`packages/mcp-servers/atlassian/`):
   - Implement tools: jira-list-issues, confluence-search-pages
   - Use Atlassian API (Jira Cloud, Confluence Cloud)
   - Serve on port 3003
3. Register all three MCPs in gateway on startup
4. Create agent templates:
   - "DevOps Agent" — combines AWS + web-search
   - "Research Agent" — combines Atlassian + web-search
5. Create seed templates with realistic system prompts

**EXECUTE**:
- Implement AWS and Atlassian MCPs
- Add docker-compose services for each
- Register MCPs on gateway startup via `/api/mcps`
- Create agent templates combining multiple MCPs
- Test agents with multi-MCP tasks

**VERIFY**:
- [ ] All three MCPs expose tools correctly
- [ ] `POST /api/mcps` or seed script registers all MCPs
- [ ] Agent templates list multiple MCP server IDs
- [ ] Agent execution accesses tools from all subscribed MCPs
- [ ] "DevOps Agent" task combines EC2 + search
- [ ] "Research Agent" task combines Jira + search
- [ ] Error handling for missing API keys
- [ ] Tests pass, no TODOs

**MOVE ON** after verification and commit.

---

### Phase 7: Async Task Queue & Webhooks

**Objective**: Add Redis and BullMQ for background task execution with webhook delivery.

**THINK**:
- How do we queue long-running agent tasks?
- How do we configure concurrency and retries?
- How do we deliver results via webhooks?
- How do we handle webhook failures and retries?

**PLAN**:
1. Add Redis service to docker-compose (port 6379)
2. Implement BullMQ queue in agent-farm:
   - Create Bull queue for agent tasks
   - Configure concurrency (default 5)
   - Set up failed job handling with retries
3. Modify task executor:
   - If `execution_mode='async'`, enqueue task to BullMQ
   - Return task ID immediately
   - Return `status='pending'` in response
4. Implement async task processor:
   - Poll queue
   - Execute agent task
   - Update task status in database
   - Call webhook_url if provided
   - Retry webhook on 5xx errors (exponential backoff)
5. Update `/api/tasks` route to support async
6. Add webhook delivery logging
7. Add admin endpoint to view queue stats

**EXECUTE**:
- Add Redis to docker-compose
- Implement BullMQ queue integration
- Update task executor for async
- Implement webhook delivery with retries
- Test async task execution and webhook delivery

**VERIFY**:
- [ ] `docker compose up` includes Redis on port 6379
- [ ] `POST /api/tasks` with `execution_mode='async'` returns task ID
- [ ] Task status transitions: pending → running → success/error
- [ ] BullMQ queue processes tasks with configured concurrency
- [ ] Webhook delivery sends results to webhook_url
- [ ] Failed webhooks retry with exponential backoff
- [ ] Queue stats visible in admin endpoint
- [ ] No TODOs, tests pass

**MOVE ON** after verification and commit.

---

### Phase 8: Observability & Admin Dashboard

**Objective**: Add structured logging, admin endpoints, usage statistics, and metrics.

**THINK**:
- How do we track usage per consumer and MCP?
- What metrics are important for monitoring?
- How do we expose admin data safely?
- What observability questions should we be able to answer?

**PLAN**:
1. Enhance logger utility:
   - Structured JSON output
   - Log levels (debug, info, warn, error)
   - Include request ID, consumer ID, duration
   - Log all API calls and tool executions
2. Create admin routes:
   - `GET /admin/usage` - Usage stats per consumer, MCP, time period
   - `GET /admin/mcps/health` - Health status of all MCPs
   - `GET /admin/tasks/stats` - Task execution statistics
   - `GET /admin/queue/stats` - Queue depth, processing times
3. Create dashboard endpoints:
   - `/admin/dashboard/summary` - High-level metrics
   - `/admin/dashboard/timeline` - Usage over time (hourly/daily)
4. Add metrics collection:
   - Tool execution count and duration
   - Agent task success/error rate
   - Queue wait time and processing time
5. Create audit log table and log all mutations

**EXECUTE**:
- Enhance logger with structured fields
- Implement admin routes with database queries
- Add metrics aggregation functions
- Test metrics calculations
- Document admin API

**VERIFY**:
- [ ] All logs are structured JSON
- [ ] `GET /admin/usage` returns per-consumer and per-MCP stats
- [ ] `GET /admin/mcps/health` shows health status
- [ ] `GET /admin/dashboard/summary` works
- [ ] Metrics are accurate and performant
- [ ] Audit logs capture all mutations
- [ ] Admin endpoints require authentication
- [ ] No TODOs, tests pass

**MOVE ON** after verification and commit.

---

### Phase 9: Hardening & Production Readiness

**Objective**: Add authentication, rate limiting, circuit breakers, graceful shutdown, and production deployment.

**THINK**:
- How do we prevent abuse and protect against unauthorized access?
- How do we handle cascading failures gracefully?
- How do we ensure a safe shutdown without losing tasks?
- What does a production docker-compose look like?

**PLAN**:
1. API Key Authentication:
   - All endpoints (except health) require Authorization header
   - Consumer API key in `Authorization: Bearer <api_key>`
   - Validate against consumers table
   - Log unauthorized attempts
2. Rate Limiting:
   - Per-consumer rate limits (from `rate_limit_per_minute`, `rate_limit_per_day`)
   - Use in-memory or Redis-backed limiter
   - Return 429 on limit exceeded
3. Circuit Breaker:
   - Protect calls to external MCPs
   - Open after 5 failures
   - Half-open after timeout
   - Return cached/default response when open
4. Graceful Shutdown:
   - Handle SIGTERM
   - Stop accepting new requests
   - Wait for in-flight requests (timeout 30s)
   - Drain BullMQ queue gracefully
   - Close database connections
5. Production docker-compose:
   - Use environment variables for secrets
   - Configure resource limits
   - Set up health checks
   - Enable logging and monitoring
6. Environment Config:
   - Separate `.env.prod` with production values
   - No hardcoded secrets
   - Load from environment variables only

**EXECUTE**:
- Implement API key validation middleware
- Add rate limiter middleware
- Implement circuit breaker for MCP calls
- Add graceful shutdown handlers
- Create docker-compose.prod.yml
- Test production deployment locally

**VERIFY**:
- [ ] All endpoints require valid API key
- [ ] Rate limit enforced per consumer
- [ ] Circuit breaker trips after failures
- [ ] Graceful shutdown completes cleanly
- [ ] No hardcoded secrets in code or config files
- [ ] Environment variables used for all sensitive data
- [ ] Production docker-compose works end-to-end
- [ ] All tests pass
- [ ] No TODOs remain in code
- [ ] All commits are clean and message-correct

**MOVE ON** - PROJECT COMPLETE

---

## API Reference

### Gateway Service (Port 8080)

#### MCP Registry Endpoints

| Method | Endpoint | Auth | Request | Response | Notes |
|--------|----------|------|---------|----------|-------|
| POST | `/api/mcps` | Key | `{name, description, transport_type, endpoint_url}` | `{id, name, ...}` | Register new MCP |
| GET | `/api/mcps` | Key | Query: `?status=healthy` | `{data: [...], total}` | List MCPs, paginated |
| GET | `/api/mcps/:id` | Key | - | `{id, name, tools, health_status}` | Get MCP details |
| PUT | `/api/mcps/:id` | Key | `{description, endpoint_url, ...}` | `{id, ...}` | Update MCP |
| DELETE | `/api/mcps/:id` | Key | - | `{success: true}` | Soft delete MCP |

#### Consumer Endpoints

| Method | Endpoint | Auth | Request | Response | Notes |
|--------|----------|------|---------|----------|-------|
| POST | `/api/consumers` | Admin | `{name, description, rate_limit_per_minute}` | `{id, name, api_key}` | Create consumer |
| GET | `/api/consumers` | Admin | - | `{data: [...], total}` | List consumers |
| GET | `/api/consumers/:id` | Key | - | `{id, name, subscriptions, usage}` | Get consumer details |

#### Subscription Endpoints

| Method | Endpoint | Auth | Request | Response | Notes |
|--------|----------|------|---------|----------|-------|
| POST | `/api/subscriptions` | Key | `{consumer_id, mcp_server_id, can_execute}` | `{id, consumer_id, mcp_server_id}` | Create subscription |
| GET | `/api/subscriptions` | Key | Query: `?consumer_id=X` | `{data: [...], total}` | List subscriptions |
| DELETE | `/api/subscriptions/:id` | Key | - | `{success: true}` | Remove subscription |

#### Tool Execution

| Method | Endpoint | Auth | Request | Response | Notes |
|--------|----------|------|---------|----------|-------|
| POST | `/api/tools/call` | Key | `{mcp: "web-search", tool: "search", params: {...}}` | `{result, execution_time_ms}` | Execute tool synchronously |
| GET | `/api/tools/call/:id` | Key | - | `{tool_call_id, status, result}` | Get tool call result |

#### Health & Metrics

| Method | Endpoint | Auth | Request | Response | Notes |
|--------|----------|------|---------|----------|-------|
| GET | `/health` | None | - | `{status: "ok", timestamp}` | Service health |
| GET | `/health/mcp` | Key | - | `{data: [{mcp_id, status, last_check}]}` | MCP health |

#### Admin Endpoints

| Method | Endpoint | Auth | Request | Response | Notes |
|--------|----------|------|---------|----------|-------|
| GET | `/admin/usage` | Admin | Query: `?period=day` | `{per_consumer, per_mcp, total_calls}` | Usage stats |
| GET | `/admin/mcps/health` | Admin | - | `{data: [{id, status, last_check_at}]}` | MCP health summary |
| GET | `/admin/dashboard/summary` | Admin | - | `{active_consumers, active_mcps, tasks_today, ...}` | Dashboard metrics |

---

### Agent Farm Service (Port 8082)

#### Agent Template Endpoints

| Method | Endpoint | Auth | Request | Response | Notes |
|--------|----------|------|---------|----------|-------|
| POST | `/api/agent-templates` | Admin | `{name, description, llm_provider, llm_model, system_prompt, mcp_server_ids}` | `{id, name, ...}` | Create template |
| GET | `/api/agent-templates` | Key | - | `{data: [...], total}` | List templates |
| GET | `/api/agent-templates/:id` | Key | - | `{id, name, system_prompt, mcp_servers}` | Get template details |
| PUT | `/api/agent-templates/:id` | Admin | `{system_prompt, temperature, ...}` | `{id, ...}` | Update template |
| DELETE | `/api/agent-templates/:id` | Admin | - | `{success: true}` | Delete template |

#### Task Endpoints

| Method | Endpoint | Auth | Request | Response | Notes |
|--------|----------|------|---------|----------|-------|
| POST | `/api/tasks` | Key | `{agent_template_id, input_prompt, execution_mode, webhook_url}` | `{id, status, result}` | Create & execute task |
| GET | `/api/tasks/:id` | Key | - | `{id, status, result, execution_time_ms, tool_calls_count}` | Get task result |
| GET | `/api/tasks` | Key | Query: `?status=success` | `{data: [...], total}` | List tasks |

#### Health & Metrics

| Method | Endpoint | Auth | Request | Response | Notes |
|--------|----------|------|---------|----------|-------|
| GET | `/health` | None | - | `{status: "ok", queue_depth, processing_time_avg}` | Service health |
| GET | `/admin/queue/stats` | Admin | - | `{queue_depth, processing_time_avg, error_rate}` | Queue statistics |

---

## Seed Data Script

Create `scripts/seed.sh` to populate initial data:

```bash
#!/bin/bash
set -e

# Colors for output
GREEN='\033[0;32m'
NC='\033[0m' # No Color

echo "Seeding MCP Farm..."

# Function to make API calls
call_api() {
  local method=$1
  local endpoint=$2
  local data=$3
  curl -s -X "$method" "http://localhost:8080$endpoint" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer admin-key" \
    -d "$data"
}

# Create consumers
echo -e "${GREEN}Creating consumers...${NC}"
call_api POST "/api/consumers" '{
  "name": "app-a",
  "description": "Application A",
  "rate_limit_per_minute": 100
}'

call_api POST "/api/consumers" '{
  "name": "app-b",
  "description": "Application B",
  "rate_limit_per_minute": 100
}'

call_api POST "/api/consumers" '{
  "name": "research-agent",
  "description": "Research Agent Consumer",
  "rate_limit_per_minute": 200
}'

call_api POST "/api/consumers" '{
  "name": "devops-agent",
  "description": "DevOps Agent Consumer",
  "rate_limit_per_minute": 200
}'

# Register MCP servers
echo -e "${GREEN}Registering MCP servers...${NC}"
call_api POST "/api/mcps" '{
  "name": "web-search",
  "description": "Web Search MCP with Brave API",
  "transport_type": "http",
  "endpoint_url": "http://web-search:3001"
}'

call_api POST "/api/mcps" '{
  "name": "aws",
  "description": "AWS SDK MCP for EC2, S3, Lambda",
  "transport_type": "http",
  "endpoint_url": "http://aws:3002"
}'

call_api POST "/api/mcps" '{
  "name": "atlassian",
  "description": "Atlassian MCP for Jira and Confluence",
  "transport_type": "http",
  "endpoint_url": "http://atlassian:3003"
}'

# Create subscriptions
echo -e "${GREEN}Creating subscriptions...${NC}"
# app-a subscribes to web-search
call_api POST "/api/subscriptions" '{
  "consumer_id": 1,
  "mcp_server_id": 1,
  "can_execute": true
}'

# research-agent subscribes to web-search and atlassian
call_api POST "/api/subscriptions" '{
  "consumer_id": 3,
  "mcp_server_id": 1,
  "can_execute": true
}'

call_api POST "/api/subscriptions" '{
  "consumer_id": 3,
  "mcp_server_id": 3,
  "can_execute": true
}'

# devops-agent subscribes to all
call_api POST "/api/subscriptions" '{
  "consumer_id": 4,
  "mcp_server_id": 1,
  "can_execute": true
}'

call_api POST "/api/subscriptions" '{
  "consumer_id": 4,
  "mcp_server_id": 2,
  "can_execute": true
}'

call_api POST "/api/subscriptions" '{
  "consumer_id": 4,
  "mcp_server_id": 3,
  "can_execute": true
}'

# Create agent templates on agent-farm
echo -e "${GREEN}Creating agent templates...${NC}"
call_api POST "/api/agent-templates" '{
  "name": "research-agent",
  "description": "Agent for research tasks using web search and Jira",
  "llm_provider": "anthropic",
  "llm_model": "claude-3-5-sonnet-20241022",
  "system_prompt": "You are a research assistant. Use the web-search and Jira tools to find information and issues. Be thorough and cite sources.",
  "mcp_server_ids": [1, 3],
  "temperature": 0.7,
  "max_tokens": 4096
}'

call_api POST "/api/agent-templates" '{
  "name": "devops-agent",
  "description": "Agent for DevOps tasks using AWS, web search, and Jira",
  "llm_provider": "anthropic",
  "llm_model": "claude-3-5-sonnet-20241022",
  "system_prompt": "You are a DevOps engineer. Use AWS tools to manage infrastructure, web-search to find documentation, and Jira to track incidents.",
  "mcp_server_ids": [1, 2, 3],
  "temperature": 0.5,
  "max_tokens": 4096
}'

echo -e "${GREEN}Seed data created successfully!${NC}"
```

---

## Final Verification Checklist

### Foundation (Phase 1)
- [ ] Git repo initialized, clean commit history
- [ ] All packages in pnpm workspace
- [ ] TypeScript strict mode, no errors
- [ ] Database migrations run, schema exists
- [ ] Logger outputs structured JSON
- [ ] Custom error classes used throughout

### Gateway REST API (Phase 2)
- [ ] All CRUD endpoints work
- [ ] Validation catches invalid input
- [ ] Mock backends respond correctly
- [ ] Soft deletes work
- [ ] Error responses include descriptive messages

### Real MCP Connections (Phase 3)
- [ ] Client pool manages multiple connections
- [ ] Health checks run every 30s
- [ ] Web-search MCP works with real Brave API
- [ ] Tool router parses namespace correctly
- [ ] Tool calls execute end-to-end

### Native MCP Server (Phase 4)
- [ ] MCP server on port 8081
- [ ] Claude Desktop can connect
- [ ] Tools discoverable via protocol
- [ ] Tool execution works through protocol

### Agent Farm (Phase 5)
- [ ] LLM provider factory works
- [ ] Tool bridge converts formats correctly
- [ ] Agent worker executes agentic loop
- [ ] Templates support multiple MCPs
- [ ] Sync task execution works

### Multiple MCPs (Phase 6)
- [ ] AWS MCP exposes correct tools
- [ ] Atlassian MCP functional
- [ ] All MCPs register and respond
- [ ] Multi-MCP agent templates work

### Async Tasks (Phase 7)
- [ ] Redis and BullMQ integrated
- [ ] Async tasks queue and process
- [ ] Webhooks deliver results
- [ ] Retries work on failures

### Observability (Phase 8)
- [ ] Structured logging everywhere
- [ ] Admin usage endpoints work
- [ ] Health dashboards accurate
- [ ] Audit logs capture mutations

### Production Hardening (Phase 9)
- [ ] API key authentication enforced
- [ ] Rate limiting works per consumer
- [ ] Circuit breakers protect MCPs
- [ ] Graceful shutdown clean
- [ ] No secrets in code/config
- [ ] Production docker-compose ready
- [ ] All tests pass
- [ ] No TODOs in code

---

## Important Rules

1. **Do NOT skip ahead** — Complete phases sequentially. Each phase builds on the previous.
2. **Do NOT leave TODOs** — Every TODO comment must be resolved before moving on.
3. **Do NOT hardcode secrets** — All API keys, passwords, URLs come from environment variables.
4. **Commit after each phase** — `git add . && git commit -m "Phase X: [description]"`
5. **When unsure, choose simpler option** — Avoid premature optimization.
6. **Test as you go** — Write tests alongside implementation.
7. **Use exact tech stack** — No substitutions without explicit reason.
8. **Document as you go** — Update CLAUDE.md with project state.

---

## Success Criteria

The project is complete when:

- All 9 phases verified and committed
- All tests passing (`pnpm -r run test`)
- No TypeScript errors (`pnpm -r run type-check`)
- No TODOs in code (`grep -r "TODO" packages/`)
- All endpoints tested and working
- Docker compose runs end-to-end without errors
- Database migrations clean and idempotent
- Code is clean, readable, and well-organized
- README explains how to use the system

Good luck! Build it with care.
