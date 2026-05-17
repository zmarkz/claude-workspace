# Portfolio Intelligence Platform — Architecture

> **Last updated**: 2026-04-08 | **Version**: 5.0 | **Author**: Claude Code + Markandey Singh

---

## 1. System Overview

An AI-first personal finance advisor for Indian retail investors. Multi-broker, multi-application platform with portfolio management across 7 Indian brokers, XIRR performance analytics, goal-based planning, tax optimization (FIFO + harvesting), net worth tracking, CDSL/NSDL CAS parsing, daily AI briefings, and document upload (Form 16, 26AS, AIS) — all orchestrated through Agent Farm.

| Metric | Value |
|--------|-------|
| Applications | 7 (3 SPAs, 1 API, 1 Gateway, 1 Orchestrator, 1 RAG Service) |
| MySQL Tables | 25+ (business data) |
| PostgreSQL Tables | 10 (MCP infrastructure + vectors + cache) |
| MCP Servers | 5 (portfolio, web-search, aws, atlassian, knowledge-store) |
| MCP Tools | 20 registered |
| Agent Templates | 6 (4 Claude, 2 Qwen local) |
| Supported Brokers | 7 (Zerodha, Upstox, Angel One, 5paisa, ICICI Direct, Groww, Paytm Money) |
| Data Import | CDSL CAS, NSDL CAS, MFCentral, Broker CSV, Form 16, 26AS, AIS, Salary Slip |
| Languages | Java 23, TypeScript 5.9 |
| Databases | MySQL 8 (business), PostgreSQL 15 + pgvector (infra), Redis 7 (queue) |
| API Endpoints | 85+ REST + 1 WebSocket + 2 MCP protocol |
| Frontend Pages | 12 (+ login) |
| Scheduled Jobs | 3 (performance 4:30PM, tax harvest Sat 9AM, briefing 6AM) |

### Test User

| Field | Value |
|-------|-------|
| Name | Rahul Sharma |
| Email | `testuser@portfolio.ai` |
| Password | `Test@123` |
| Portfolios | 2 (Long Term Wealth: 10 stocks, Smallcap Bets: 3 stocks) |
| Net Worth | ~₹30.4L (equity + MF + FD + PPF + Gold + Savings) |
| Risk Profile | MODERATE (7/10) |
| Test Guide | See `docs/TEST_CASES.md` |

---

## 2. High-Level Architecture

```
                                ┌──────────────┐
                                │   BROWSER    │
                                └──────┬───────┘
                                       │
                                ┌──────▼───────┐
                                │    NGINX     │ :3000
                                │  /        → Frontend (:5173)
                                │  /api/*   → Backend  (:8080)
                                │  /ws      → WebSocket(:8080)
                                │  /admin/* → Admin SPA(:5174)
                                │  /gateway → MCP GW   (:9080)
                                │  /agent   → Agent Farm(:8082)
                                │  /me      → markandey.in(:5175)
                                └──┬──┬──┬──┬──┘
               ┌───────────────────┘  │  │  └──────────────────┐
               │                      │  │                     │
      ┌────────▼────────┐    ┌───────▼──▼───────┐    ┌───────▼─────────┐
      │  Portfolio       │    │  Portfolio       │    │  Admin Nexus    │
      │  Frontend        │    │  Tracker API     │    │  (React SPA)   │
      │  React (:5173)   │    │  Spring Boot     │    │  :5174         │
      │                  │    │  (:8080)         │    └───────┬─────────┘
      │ - Dashboard      │    │                  │            │
      │ - Holdings       │    │ - Auth (JWT)     │            │
      │ - AI Assessment  │    │ - Portfolios     │            │
      │ - Tax Dashboard  │    │ - AI Analysis    │    ┌───────▼─────────┐
      │ - Income/Trades  │    │ - Tax Engine     │    │  MCP Gateway    │
      │ - Transactions   │    │ - Income CRUD    │    │  Fastify        │
      └─────────┬────────┘    │ - Approvals      │    │  :9080/:8081    │
                │             │ - Kite Sync      │    │                 │
                │             └────┬──────┬──────┘    │ - Tool Registry │
                │                  │      │           │ - Consumer Auth │
                │                  │      │           │ - Circuit Breaker│
                ▼                  │      │           └────────┬────────┘
           ┌─────────┐            │      │                    │
           │ MySQL 8 │            │  ┌───▼──────────┐  ┌─────┼──────────────┐
           │ :3306   │            │  │ Agent Farm   │  │     │              │
           └─────────┘            │  │ Fastify      │  │  ┌──▼──┐  ┌──────▼───┐
                                  │  │ :8082        │  │  │PT   │  │Web Search│
                                  │  │              │  │  │MCP  │  │MCP       │
                                  │  │ - LLM Calls  │  │  │:3004│  │:3001     │
                                  │  │ - BullMQ     │  │  └─────┘  └──────────┘
                                  │  │ - Streaming  │  │
                                  │  └──────┬───────┘  │
                                  │         │          │
                             ┌────▼─────┐ ┌─▼──────┐ ┌─▼──────────┐
                             │Anthropic │ │ Redis  │ │ PostgreSQL │
                             │Claude API│ │ :6379  │ │ :5432      │
                             │(External)│ └────────┘ │ + pgvector │
                             └──────────┘            └─────┬──────┘
                                                           │
                                                    ┌──────▼──────┐
          Portfolio API ────────────────────────────►│ Knowledge   │
          (KnowledgeStoreClient)                     │ Store       │
                                                     │ Fastify     │
                                                     │ :3010       │
                                                     │             │
                                                     │ - pgvector  │
                                                     │ - Embeddings│◄─── Ollama nomic-embed-text
                                                     │ - Cache     │     (FREE, 768 dims)
                                                     │ - MCP Server│
                                                     └─────────────┘
```

---

## 3. Service Catalog

| # | Service | Type | Stack | Port | Container | Purpose |
|---|---------|------|-------|------|-----------|---------|
| 1 | Portfolio Frontend | React SPA | React 19, Vite, TailwindCSS, Zustand | 5173 | `portfolio_tracker_frontend` | User dashboard |
| 2 | Portfolio Tracker API | REST API | Java 23, Spring Boot 3.2, JPA, Maven | 8080 | `portfolio_tracker_api` | Business logic |
| 3 | Admin Nexus | React SPA | React 19, Vite, TailwindCSS, Recharts | 5174 | `admin_nexus` | MCP Farm admin |
| 4 | MCP Gateway | API + MCP | Node.js, Fastify, Drizzle, MCP SDK | 9080/8081 | `mcp_gateway` | Tool routing |
| 5 | Agent Farm | Orchestrator | Node.js, Fastify, Vercel AI SDK, BullMQ | 8082 | `agent_farm` | AI execution |
| 6 | Portfolio MCP | MCP Server | Node.js, TypeScript, MCP SDK | 3004 | `mcp_portfolio_tracker` | Portfolio tools |
| 7 | Web Search MCP | MCP Server | Node.js, TypeScript, MCP SDK | 3001 | `mcp_web_search` | Search tools |
| 8 | Nginx | Reverse Proxy | Nginx | 3000 | `platform_nginx` | Routing |
| 9 | Redis | Queue/Cache | Redis 7 Alpine | 6379 | `platform_redis` | BullMQ |
| 10 | MySQL | Database | MySQL 8.0 | 3306 | Host | Business data |
| 11 | PostgreSQL | Database | PostgreSQL 15 + pgvector | 5432 | Host | MCP metadata + vectors |
| 12 | Knowledge Store | RAG Service | Node.js, Fastify, pgvector, Ollama | 3010 | `knowledge_store` | Semantic search + cache |
| 13 | markandey.in | Next.js SSR | Next.js 15, React 19, Tailwind 4, Framer Motion | 5175 | `markandey_in` | Personal website + portfolio |
| 14 | Cloudflared | Tunnel | Cloudflare Tunnel | — | `cloudflared` | Public access via rakha.xyz |

---

## 4. Authentication Architecture

### 7 Layers of Authentication

```
Layer 1: User Auth (JWT)
  Who:   End users (portfolio owners)
  How:   POST /api/auth/login → JWT (HS256, 24hr expiry)
  Where: Authorization: Bearer <jwt>
  Check: JwtTokenFilter on every request

Layer 2: Service-to-Service
  Who:   Portfolio API → Agent Farm
  How:   AGENT_FARM_API_KEY (env var)
  Where: Authorization: Bearer <api-key>

Layer 3: MCP Consumer
  Who:   Agent Farm → MCP Gateway
  How:   Consumer API key from consumers table
  Where: Authorization: Bearer <consumer-key>
  Check: Subscription ACL (canRead + canExecute)

Layer 4: Admin
  Who:   Admin Nexus → Gateway + Agent Farm
  How:   ADMIN_API_KEY (default: "admin-secret")
  Where: Authorization: Bearer <admin-key>

Layer 5: MCP Server Auth
  Who:   Portfolio MCP → Portfolio API
  How:   Service account JWT (login on startup)

Layer 6: Ownership Validation
  Who:   Authenticated user → only their data
  How:   verifyPortfolioOwnership() in AIController
  Check: SecurityContextHolder → userId == portfolio.userId

Layer 7: External APIs
  Anthropic: x-api-key header
  Zerodha:   OAuth 2.0 flow
```

---

## 5. Data Flow: AI Portfolio Analysis

```
Browser                 Portfolio API          Agent Farm         MCP Gateway        Claude API
  │                          │                     │                  │                  │
  │ POST /api/ai/            │                     │                  │                  │
  │ portfolio/1/analyze      │                     │                  │                  │
  │─────────────────────────>│                     │                  │                  │
  │                          │                     │                  │                  │
  │                 1. Verify Ownership             │                  │                  │
  │                    (JWT → userId check)         │                  │                  │
  │                          │                     │                  │                  │
  │                 2. Build Portfolio Context      │                  │                  │
  │                    (holdings, P&L, sectors)     │                  │                  │
  │                          │                     │                  │                  │
  │                 3. Check Agent Farm             │                  │                  │
  │                    GET /health (3s timeout)     │                  │                  │
  │                          │────────────────────>│                  │                  │
  │                          │                     │                  │                  │
  │                    ┌─────┴── Available? ───┐   │                  │                  │
  │                    │ YES                   │NO │                  │                  │
  │                    │                       │   │                  │                  │
  │                    │ POST /api/tasks       │   │                  │                  │
  │                    │ {templateId:4,        │   │                  │                  │
  │                    │  mode:"sync"}         │   │                  │                  │
  │                    │──────────────────────>│   │                  │                  │
  │                    │                       │   │                  │                  │
  │                    │            LLM + tools│   │  Direct Claude   │                  │
  │                    │                       │   │  API call with   │                  │
  │                    │  Calls MCP tools:     │   │  inline JSON     │                  │
  │                    │  portfolio-tracker__*  │   │  schema          │                  │
  │                    │  web-search__*         │   │                  │                  │
  │                    │                       │   │──────────────────────────────────>│
  │                    │  Structured JSON       │   │                  │                  │
  │                    │<─────────────────────│   │  Structured JSON │                  │
  │                    │                       │   │<──────────────────────────────────│
  │                    └───────────────────────┘   │                  │                  │
  │                          │                     │                  │                  │
  │                 4. Save to analysis_history     │                  │                  │
  │                    (model, prompt, response)    │                  │                  │
  │                          │                     │                  │                  │
  │  {healthScore: 82,       │                     │                  │                  │
  │   riskProfile: "Moderate",                     │                  │                  │
  │   risks: [...],          │                     │                  │                  │
  │   recommendations: [...]}│                     │                  │                  │
  │<─────────────────────────│                     │                  │                  │
```

---

## 6. Data Flow: Tax Calculation (FIFO Lot Matching)

```
Input: Financial Year "2025-26" (April 2025 → March 2026)

Step 1: Fetch SELL trades in FY
  ┌──────────────────────────────────────────────────┐
  │ SELL  RELIANCE  2025-06-10  10 shares @ ₹2,900  │
  │ SELL  TCS       2025-11-20   5 shares @ ₹3,800  │
  └──────────────────────────────────────────────────┘

Step 2: Fetch ALL BUY trades for sold symbols (FIFO queue)
  ┌──────────────────────────────────────────────────┐
  │ BUY   RELIANCE  2025-01-15  10 shares @ ₹2,650  │
  │ BUY   TCS       2025-03-20   5 shares @ ₹3,450  │
  └──────────────────────────────────────────────────┘

Step 3: FIFO Match (earliest buy consumed first)
  ┌──────────────────────────────────────────────────┐
  │ RELIANCE: BUY 2025-01-15 → SELL 2025-06-10      │
  │   Holding: 146 days (< 365) → STCG              │
  │   Gain: (₹2,900 - ₹2,650.50) × 10 = ₹2,495    │
  │                                                   │
  │ TCS: BUY 2025-03-20 → SELL 2025-11-20           │
  │   Holding: 245 days (< 365) → STCG              │
  │   Gain: (₹3,800 - ₹3,450) × 5 = ₹1,750         │
  └──────────────────────────────────────────────────┘

Step 4: Apply Indian Tax Rules
  ┌──────────────────────────────────────────────────┐
  │ LTCG (> 365 days):  12.5% above ₹1.25L exempt   │
  │   Total: ₹0 → Tax: ₹0                           │
  │                                                   │
  │ STCG (≤ 365 days): 20% flat                     │
  │   Total: ₹4,245 → Tax: ₹849                     │
  └──────────────────────────────────────────────────┘

Step 5: Aggregate Income
  ┌──────────────────────────────────────────────────┐
  │ Dividends:    ₹5,000   (TDS: ₹500)              │
  │ FD Interest:  ₹12,000  (TDS: ₹1,200)            │
  │ Total Income: ₹21,245  Total TDS: ₹1,700        │
  └──────────────────────────────────────────────────┘
```

### Indian Tax Rules (Post Budget 2024)

| Asset Class | Holding Period | Tax Rate | Exemption |
|-------------|---------------|----------|-----------|
| Listed Equity (STT paid) | > 12 months | 12.5% LTCG | ₹1.25 Lakh/FY |
| Listed Equity (STT paid) | ≤ 12 months | 20% STCG | None |
| Debt Mutual Fund | Any | Slab rate | No indexation |
| SGB (maturity) | At maturity | 0% | Tax-free |
| Dividends | N/A | Slab rate | TDS u/s 194 |
| FD Interest | N/A | Slab rate | TDS u/s 194A |

---

## 7. Data Flow: MCP Tool Execution

```
Agent Farm          MCP Gateway           Client Pool          MCP Server         Portfolio API
  │                     │                     │                    │                   │
  │ POST /api/tools/    │                     │                    │                   │
  │ call {tool:         │                     │                    │                   │
  │  "portfolio-tracker │                     │                    │                   │
  │   __get_holdings",  │                     │                    │                   │
  │  args:{userId:1}}   │                     │                    │                   │
  │────────────────────>│                     │                    │                   │
  │                     │ 1. Parse tool path  │                    │                   │
  │                     │    mcp: "portfolio- │                    │                   │
  │                     │     tracker"        │                    │                   │
  │                     │    tool: "get_      │                    │                   │
  │                     │     holdings"       │                    │                   │
  │                     │                     │                    │                   │
  │                     │ 2. Check consumer   │                    │                   │
  │                     │    subscription     │                    │                   │
  │                     │    (canExecute)     │                    │                   │
  │                     │                     │                    │                   │
  │                     │ 3. Route to pool    │                    │                   │
  │                     │────────────────────>│ Lazy init client  │                   │
  │                     │                     │───────────────────>│                   │
  │                     │                     │   JSON-RPC call    │                   │
  │                     │                     │                    │ JWT auth + GET    │
  │                     │                     │                    │ /api/portfolios/  │
  │                     │                     │                    │ 1/holdings        │
  │                     │                     │                    │──────────────────>│
  │                     │                     │                    │  Holdings JSON    │
  │                     │                     │                    │<──────────────────│
  │                     │                     │  Tool result       │                   │
  │                     │                     │<───────────────────│                   │
  │                     │<────────────────────│                    │                   │
  │                     │                     │                    │                   │
  │                     │ 4. Audit log        │                    │                   │
  │                     │    (tool_calls tbl) │                    │                   │
  │  Tool result JSON   │                     │                    │                   │
  │<────────────────────│                     │                    │                   │
```

---

## 8. Knowledge Store & Smart Context

### Knowledge Store Service (port 3010)

Shared RAG + caching service using pgvector in PostgreSQL. Provides semantic search, key-value cache, and pre-computed digests. Serves portfolio-tracker now, any future app later.

```
Portfolio Tracker API                        Knowledge Store (:3010)
  │                                                │
  ├── KnowledgeStoreClient.java                    ├── POST /store    → embed + save to pgvector
  │   ├── store(userId, category, content)    ───► │
  │   ├── search(userId, query, limit)        ───► ├── POST /query    → cosine similarity search
  │   ├── searchSessionInsights(userId, q)    ───► │
  │   ├── storeSessionInsight(userId, text)   ───► │
  │   ├── getCachedResponse(userId, hash)     ───► ├── GET /cache/get → key-value lookup
  │   ├── cacheResponse(userId, hash, resp)   ───► ├── POST /cache/set→ store with TTL
  │   ├── getDigest(userId, type)             ───► ├── GET /digest    → pre-computed summary
  │   └── saveDigest(userId, type, content)   ───► └── POST /digest   → upsert summary
  │
  └── Embedding: Ollama nomic-embed-text (768 dims, FREE)
```

### 3-Layer Smart Context Management

Replaces blind `messages.slice(-10)` with curated context. Reduces Claude input tokens ~42%.

```
BEFORE: [10 raw messages ~2500tok] + [portfolio ~800tok] → LLM  (~3650 input tokens)
AFTER:  [summary ~200tok] + [3 messages ~600tok] + [insights ~150tok] + [portfolio ~800tok] → LLM (~1800 tokens)
```

**Layer 1 — Rolling Summary**: After every 3rd turn, Qwen local (FREE) summarizes the conversation → `chat_sessions.summary` column.

**Layer 2 — Session Insights**: After each COMPLEX query, Qwen extracts key facts → stored in Knowledge Store (`session_insight` category) for cross-session semantic retrieval.

**Layer 3 — Smart Context Window**: `AIAnalysisService.buildSmartContext()` assembles:
1. Session summary (if ≥3 turns) — ~200 tokens
2. Last 3 raw turns from current session — ~600 tokens
3. Knowledge Store insights (semantic search, top 3) — ~150 tokens
4. Current message
5. Portfolio data (always in systemOverride) — ~800 tokens

**Session Lifecycle**:
```
New Chat → createSession()
  │
  ├── Turn 1-2: raw messages only (no summary yet)
  ├── Turn 3: triggerSummaryIfNeeded() → Qwen summarizes → chat_sessions.summary
  ├── Turn 6: rolling summary updated (integrates new messages)
  │
  ├── COMPLEX query: extractSessionInsights() → Knowledge Store
  │
  └── New Chat clicked → finalizeSession()
       ├── Final summary via Qwen (≤300 words)
       ├── Stored in chat_sessions.summary
       └── Stored in Knowledge Store for cross-session retrieval
```

**Cost Impact**: Monthly Claude spend ~₹46 → ~₹28 (39% reduction). Qwen calls: ₹0 (local). Embeddings: ₹0 (Ollama).

### Hybrid Document Parsing (Rigid + AI Fallback)

All file uploads use a two-tier approach for maximum reliability:

1. **Rigid parser** tries first — column matching, regex extraction (~100ms)
2. If <50% success rate → **Qwen AI fallback** (FREE via Ollama, 15-30s) parses semantically

| Document Type | Parser Service | AI Fallback | Output |
|---------------|---------------|-------------|--------|
| Holdings CSV/XLSX | `CsvUploadService` | No | PortfolioHolding rows |
| Tradebook CSV/XLSX | `TradebookParserService` | Yes (Qwen) | TradebookEntry rows |
| Salary Slip (any format) | `SalarySlipParserService` | Always (Qwen) | IncomeEntry + component JSON |
| Zerodha Ledger | `LedgerParserService` | No | IncomeEntry (dividends) |
| P&L Statement | `PnlStatementParserService` | No | Per-stock realized gains |
| Form 16/26AS/AIS | `DocumentParserService` | No | IncomeEntry rows |
| CDSL/NSDL CAS | `CdslCasParserService` | No | Holdings + MF holdings |

---

## 9. Database Schema

### MySQL — `portfolio_tracker` (25+ tables)

```
users ─────────┬─── portfolios ──────┬─── portfolio_holdings ──── stocks
  │             │                     │
  │             │                     └─── transactions
  │             │
  ├─── user_preferences (1:1)        ┌─── stock_price_history
  ├─── income_entries                 │
  ├─── tradebook_entries              stocks ────┘
  ├─── analysis_history
  ├─── chat_sessions ──── chat_history financial_goals ──── goal_allocations
  ├─── approval_requests
  ├─── financial_goals                net_worth_assets
  ├─── net_worth_assets               mutual_fund_holdings
  ├─── mutual_fund_holdings           performance_snapshots
  ├─── notifications                  tax_harvest_opportunities
  ├─── daily_briefings                daily_briefings
  ├─── watchlists ──── watchlist_items
  └─── alerts
```

| Table | Key Fields | Purpose |
|-------|-----------|---------|
| `users` | id, email, password, role (USER/ADMIN), kite_token | Authentication |
| `portfolios` | id, user_id, name, currency | Portfolio containers |
| `portfolio_holdings` | portfolio_id, stock_id, quantity, avg_buy_price | Current positions |
| `stocks` | symbol, company_name, sector, current_price | Stock master |
| `transactions` | portfolio_id, stock_id, type (BUY/SELL), quantity, price | Trade log |
| `stock_price_history` | stock_id, date, open, high, low, close | Historical prices |
| `alerts` | user_id, stock_id, condition, threshold | Price alerts |
| `income_entries` | user_id, type, amount, financial_year, tax_deducted | Investment income |
| `tradebook_entries` | user_id, symbol, trade_date, trade_type, price, order_id | Kite trade history |
| `analysis_history` | user_id, portfolio_id, type, ai_model, raw_response, health_score | AI analysis audit |
| `chat_sessions` | user_id, portfolio_id, title, turn_count, summary | Session grouping |
| `chat_history` | user_id, portfolio_id, session_id, role, content | Chat persistence |
| `approval_requests` | user_id, portfolio_id, status, payload, review_notes | Approval workflow |

### PostgreSQL — `mcp_farm` (10 tables, including pgvector)

| Table | Purpose |
|-------|---------|
| `mcp_servers` | Registered MCP server endpoints and health |
| `tool_definitions` | Cached tool schemas (name, description, inputSchema) |
| `consumers` | API consumers with keys and rate limits |
| `subscriptions` | Consumer → MCP server access (canRead/canExecute) |
| `tool_calls` | Audit log of every tool execution |
| `agent_templates` | LLM configs (model, system prompt, MCP associations) |
| `agent_tasks` | Task execution records (input, output, status, time) |
| `knowledge_entries` | RAG entries with pgvector embeddings (768-dim), category, source |
| `cache_entries` | Key-value cache with TTL (response caching) |
| `digests` | Pre-computed portfolio/tax/goals summaries |

---

## 9. MCP Tool Registry

| MCP Server | Tool | User-Scoped | Description |
|------------|------|-------------|-------------|
| portfolio-tracker (:3004) | `list_portfolios` | Yes | List user portfolios |
| | `get_portfolio_holdings` | Yes | Holdings with gain/loss |
| | `get_portfolio_metrics` | Yes | Sector allocation, top holdings |
| | `get_portfolio_summary` | Yes | Full portfolio view |
| | `get_portfolio_transactions` | Yes | Transaction history |
| | `get_recent_transactions` | Yes | Recent trades |
| | `search_stocks` | No | Symbol/name search |
| | `get_stock_price` | No | Current NSE price |
| | `get_stock_analysis` | No | Technical indicators |
| | `get_tax_summary` | Yes | LTCG/STCG breakdown |
| | `get_income_entries` | Yes | Recorded income |
| web-search (:3001) | `web_search` | No | Brave/DuckDuckGo search |
| | `get_page_content` | No | Extract text from URL |
| knowledge-store (:3010) | `knowledge_search` | Yes | Semantic search past analyses/insights |
| | `knowledge_get_digest` | Yes | Get pre-computed portfolio/tax digest |
| | `knowledge_store_fact` | Yes | Store user preference/decision |

---

## 10. Agent Templates

| ID | Name | Provider | Model | MCP Servers | Output | Use Case |
|----|------|----------|-------|-------------|--------|----------|
| 1 | Portfolio Analyst | anthropic | claude-sonnet-4-6 | portfolio-tracker, web-search | Free-form text | General analysis |
| 2 | Research Agent | anthropic | claude-3.5-sonnet | web-search | Research synthesis | Web research |
| 3 | **Portfolio Analyst (COMPLEX)** | **anthropic** | **claude-sonnet-4-6** | portfolio-tracker, web-search | **Structured JSON** | **COMPLEX queries (sell/buy/analyze)** |
| 4 | **Local Portfolio Analyst (SIMPLE)** | **ollama** | **qwen2.5-coder:14b** | — | **Streaming markdown** | **SIMPLE queries, briefings, summaries (FREE)** |
| 5 | Rebalancing Agent | anthropic | claude-sonnet-4-6 | portfolio-tracker, web-search | JSON array (current/target weights) | Portfolio rebalancing |
| 6 | Tax Advisor | anthropic | claude-sonnet-4-6 | portfolio-tracker | Indian tax optimization | Tax planning |

**AI Model Routing**: `AIAnalysisService.classifyQuery()` routes each user message:
- **COMPLEX** (analyze, sell, buy, recommend, rebalance, risk, tax, harvest) → Template 3 (Claude, ~₹0.03-0.08/call)
- **SIMPLE** (what is, how many, show me, list, explain, summary) → Template 4 (Qwen local, ₹0/call)
- **DEFAULT** → COMPLEX (safer fallback)
- **~93% of queries** go to Qwen local (free), **~7%** to Claude

---

## 11. API Endpoint Map (85+ endpoints)

### Auth (Public)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login → JWT token |
| POST | `/api/auth/google` | Google OAuth sign-in (validates ID token, creates/links account) |

### Portfolio Management
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/portfolios` | List user's portfolios |
| POST | `/api/portfolios` | Create portfolio |
| GET | `/api/portfolios/{id}` | Get portfolio by ID |
| GET | `/api/portfolios/{id}/holdings` | Get holdings |
| POST | `/api/portfolios/{id}/holdings` | Add holding |
| DELETE | `/api/portfolios/{pid}/holdings/{hid}` | Remove holding |
| GET | `/api/portfolios/{id}/analysis` | Portfolio analysis view |
| POST | `/api/portfolios/{id}/holdings/upload` | Upload CSV/XLSX |

### Stocks
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/stocks` | List/search stocks |
| GET | `/api/stocks/{id}` | Get stock details |
| GET | `/api/stocks/{id}/history` | Price history |

### Transactions
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/transactions` | List transactions |
| POST | `/api/transactions` | Record transaction |
| POST | `/api/transactions/upload` | Upload CSV/XLSX |

### AI Analysis (Ownership Protected)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/ai/portfolio/{id}/analyze` | Full AI analysis |
| POST | `/api/ai/portfolio/{id}/chat` | Chat (SSE streaming, COMPLEX/SIMPLE routing) |
| GET | `/api/ai/portfolio/{id}/rebalance` | Rebalancing suggestions |
| GET | `/api/ai/portfolio/{id}/analysis/history` | Analysis history |
| GET | `/api/ai/portfolio/{id}/analysis/latest` | Latest analysis |
| GET | `/api/ai/portfolio/{id}/rebalancing/history` | Rebalancing history |
| GET | `/api/ai/portfolio/{id}/rebalancing/latest` | Latest rebalancing |
| GET | `/api/ai/analysis/{analysisId}` | Full transparency detail |
| GET | `/api/ai/portfolio/{pid}/stock/{symbol}` | Stock recommendation |
| GET | `/api/ai/status` | AI config status |

### Chat Sessions
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/ai/portfolio/{id}/chat/sessions` | Create new chat session |
| GET | `/api/ai/portfolio/{id}/chat/sessions` | List sessions (newest first) |
| GET | `/api/ai/portfolio/{id}/chat/sessions/{sid}/messages` | Get session messages |
| PUT | `/api/ai/portfolio/{id}/chat/sessions/{sid}` | Update session title |
| POST | `/api/ai/portfolio/{id}/chat/sessions/{sid}/finalize` | Finalize session (summary → Knowledge Store) |
| GET | `/api/ai/portfolio/{id}/chat/history` | Get all chat history (flat, backward-compat) |
| POST | `/api/ai/portfolio/{id}/chat/history` | Save chat pair (supports optional sessionId) |
| DELETE | `/api/ai/portfolio/{id}/chat/history` | Clear chat |

### Tax & Tradebook
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/tax/{fy}/summary` | Capital gains tax summary (LTCG/STCG, FIFO) |
| GET | `/api/tax/{fy}/opportunities` | Computed tax-saving suggestions (LTCG exemption, 80C, harvesting) |
| GET | `/api/tax/{fy}/regime-comparison` | Compare Old vs New tax regime based on salary data |
| POST | `/api/tax/tradebook/upload` | Upload tradebook CSV/XLSX (hybrid: rigid + Qwen AI fallback) |
| GET | `/api/tax/tradebook` | List tradebook entries |

### Tax-Loss Harvesting
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/tax-harvest/opportunities` | List active harvest opportunities |
| POST | `/api/tax-harvest/scan` | Scan portfolio for harvestable losses (>₹500) |
| PUT | `/api/tax-harvest/opportunities/{id}/dismiss` | Dismiss opportunity |
| GET | `/api/tax-harvest/opportunities/{id}/what-if` | What-if analysis (proceeds, loss, saving) |

### Document Upload
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/documents/upload` | Upload Form 16/26AS/AIS/CAS/Salary Slip (AI-parsed) |
| GET | `/api/documents` | List uploaded documents |

### Performance
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/performance/portfolio/{id}?period=1Y` | Enriched metrics (winners/losers/sector/tax/completeness) |
| POST | `/api/performance/portfolio/{id}/snapshot` | Trigger performance snapshot |

### Income Tracking
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/income` | List income entries |
| POST | `/api/income` | Add income entry |
| DELETE | `/api/income/{id}` | Delete income entry |

### Mutual Funds
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/mutual-funds` | List MF holdings |

### Goals & SIP
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/goals` | List financial goals |
| POST | `/api/goals` | Create goal |
| PUT | `/api/goals/{id}` | Update goal |
| DELETE | `/api/goals/{id}` | Delete goal |

### Net Worth
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/networth` | Get multi-asset net worth |
| POST | `/api/networth/assets` | Add asset |
| PUT | `/api/networth/assets/{id}` | Update asset |
| DELETE | `/api/networth/assets/{id}` | Delete asset |

### Watchlist & Alerts
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/watchlists` | List watchlists |
| POST | `/api/watchlists` | Create watchlist |
| POST | `/api/watchlists/{id}/items` | Add item |
| DELETE | `/api/watchlists/{id}/items/{itemId}` | Remove item |

### Notifications
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/notifications` | List notifications |
| PUT | `/api/notifications/{id}/read` | Mark as read |

### Preferences
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/preferences` | Get user preferences + risk profile |
| PUT | `/api/preferences` | Update preferences |

### Daily Briefings
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/briefings/latest` | Get latest daily briefing |
| POST | `/api/briefings/generate` | Trigger briefing generation |

### Approval Workflow
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/approvals` | List approval requests |
| GET | `/api/approvals/{id}` | Get approval detail |
| POST | `/api/approvals/{id}/approve` | Approve request |
| POST | `/api/approvals/{id}/reject` | Reject request |

### Multi-Broker Auth
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/broker/auth-url/{broker}` | Get OAuth URL for any broker |
| POST | `/api/broker/callback/{broker}` | Handle broker OAuth callback |
| GET | `/api/broker/connections` | List connected brokers |
| DELETE | `/api/broker/connections/{id}` | Disconnect broker |
| POST | `/api/broker/sync/{broker}` | Sync holdings from broker |

### Zerodha Kite (Legacy)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/kite/auth-url` | Get Kite OAuth URL |
| POST | `/api/kite/callback` | OAuth callback |
| POST | `/api/kite/sync` | Sync holdings |

### WebSocket & Docs
| Path | Description |
|------|-------------|
| `/ws` | STOMP over SockJS (subscribe: `/topic/prices`) |
| `/swagger-ui.html` | Interactive API docs |
| `/v3/api-docs` | OpenAPI 3.0 spec |

---

## 12. Frontend Pages

| Route | Page | Features |
|-------|------|----------|
| `/login` | Login/Register | Email + password auth, **Google OAuth sign-in** |
| `/dashboard` | Dashboard | Holdings table, metrics bar, sector chart, **Import Data button** (→ ImportGuide modal), Kite sync |
| `/holdings` | Holdings | Stock list, click through to analysis |
| `/performance` | Performance | **8 metric cards** (returns, day change, holdings, top sector, winners, losers, tax, dividends), data completeness bar with upload CTAs, portfolio value chart, sector chart |
| `/stocks/:symbol` | Stock Analysis | Price, SMA50/200, RSI, MACD, AI insights |
| `/transactions` | Transactions | Buy/sell execution, history |
| `/ai` | AI Assessment | **Chat with session sidebar** (new chat, switch session, finalize), fullscreen toggle, structured JSON responses (cards/tables/badges), streaming markdown, **3 tabs**: Chat, Analysis, Rebalance |
| `/goals` | Goals | Goal cards, progress bars, SIP calculator, 3-scenario projections |
| `/networth` | Net Worth | Multi-asset total, donut chart, asset CRUD, liquidity breakdown |
| `/watchlist` | Watchlist | Stocks with target prices, alerts, live price comparison |
| `/tax` | Tax Dashboard | **4 tabs**: Summary (LTCG/STCG cards + capital gains table), **Harvesting** (scan + what-if), **Opportunities** (suggestions + **regime comparison** Old vs New), **AI Advisor** (pre-built tax questions → Claude) |
| `/income` | Income & Trades | Income CRUD (7 types), Tradebook CSV upload (hybrid parser), tabbed UI |
| `/settings` | Settings | Risk questionnaire (10-step), preferences, notification toggles |

### Key UI Components

| Component | Location | Purpose |
|-----------|----------|---------|
| **ImportGuide** | `components/import/ImportGuide.tsx` | Central modal with 3 tabs (Holdings, Tradebook, Tax Documents), 14 import sources, drag-and-drop upload |
| **StructuredResponse** | `components/ai/StructuredResponse.tsx` | Renders COMPLEX query JSON → SummaryCard, tables, badges, warnings, disclaimer |
| **MarkdownMessage** | `components/ai/MarkdownMessage.tsx` | Streams SIMPLE query responses as markdown |
| **Collapsible Sidebar** | `components/layout/Sidebar.tsx` | Full (240px) ↔ icons-only (64px), auto-collapses on AI page, persisted in localStorage |
| **DataCompleteness** | `components/performance/DataCompleteness.tsx` | Progress bar showing data quality with upload CTAs |
| **TaxHarvestPanel** | `components/tax/TaxHarvestPanel.tsx` | Scan + harvest opportunity cards + what-if analysis |

---

## 13. Resilience Patterns

| Pattern | Implementation |
|---------|---------------|
| AI Fallback | Agent Farm first → Direct Claude API if unavailable |
| Circuit Breaker | MCP Gateway: 5 failures → 60s open → half-open retry |
| Connection Pool | Lazy init per MCP server, stale session eviction |
| Task Queue | BullMQ: 5 workers, 3 retries, exponential backoff |
| Agent Limits | 120s timeout, max 10 tool-use steps |
| Data Import Fallback | Kite API → CSV upload → Paste CSV text |
| Health Monitoring | 30s health check loop for all MCP servers |

---

## 14. Approval Workflow State Machine

```
              ┌─────────┐
 AI generates │ PENDING │
 suggestion   └────┬────┘
                   │
        ┌──────────┼──────────┐
        │          │          │
   ┌────▼────┐     │    ┌────▼─────┐
   │APPROVED │     │    │REJECTED  │
   └────┬────┘     │    └──────────┘
        │          │
   ┌────▼────┐  ┌──▼──────┐
   │EXECUTED │  │ EXPIRED │
   │(Future) │  │ (TTL)   │
   └─────────┘  └─────────┘
```

---

## 15. Deployment

### Local Development (Docker Compose)
```
Host Machine (macOS)
├── MySQL 8 (:3306)           ← native
├── PostgreSQL 15 (:5432)     ← native
├── Ollama (nomic-embed-text) ← native, for embeddings (FREE)
└── Docker Desktop
    └── platform_network (bridge)
        ├── redis (:6379)
        ├── nginx (:3000)
        ├── portfolio-tracker (:8080)
        ├── portfolio-tracker-frontend (:5173)
        ├── mcp-gateway (:9080/:8081)
        ├── agent-farm (:8082)
        ├── mcp-portfolio-tracker (:3004)
        ├── mcp-web-search (:3001)
        ├── knowledge-store (:3010)
        ├── admin-nexus (:5174)
        ├── markandey-in (:5175)
        └── cloudflared (tunnel → rakha.xyz)
```

### Cloudflare Tunnel (Public Access)
```
cloudflared tunnel run rakha
  ├── https://rakha.xyz     → nginx (:3000)  → all services
  ├── https://api.rakha.xyz → :8080          → API direct
  └── https://admin.rakha.xyz → :5174        → Admin Nexus direct
```

### Production (AWS)
```
EC2 (t3.small)     → Docker Compose (all services) + Nginx SSL
RDS MySQL          → portfolio database
RDS PostgreSQL     → mcp_farm database
ElastiCache Redis  → BullMQ queue
S3                 → Frontend assets, DB backups
```

---

## 16. Technology Stack

| Layer | Technologies |
|-------|-------------|
| Backend | Java 23, Spring Boot 3.2, Maven, JPA/Hibernate, MySQL 8 |
| Frontend | React 19, TypeScript 5.9, Vite 8, TailwindCSS 4, Zustand 5, React Query 5 |
| Infrastructure | Node.js 18, Fastify v4, Drizzle ORM, PostgreSQL 15 |
| AI | Anthropic Claude Sonnet 4.6 (COMPLEX), Qwen 2.5 Coder 14B via Ollama (SIMPLE/FREE), Vercel AI SDK v6 |
| RAG | pgvector (cosine similarity), Ollama nomic-embed-text (768-dim, FREE) |
| Personal Site | Next.js 15, Framer Motion, Tailwind 4 |
| MCP | @modelcontextprotocol/sdk v1.29, StreamableHTTP transport |
| Queue | BullMQ + Redis 7 |
| Broker | Zerodha Kite Connect (OAuth 2.0) |
| Charts | Recharts 3.8 |
| Real-time | STOMP over SockJS, SSE (Server-Sent Events) |
| Containers | Docker, Docker Compose, Nginx |
| API Docs | SpringDoc OpenAPI 3.0 + Swagger UI |

---

## 17. Design Principles

| Principle | Implementation |
|-----------|---------------|
| Apps own data, Agent Farm owns intelligence | Portfolio API manages domain data; Agent Farm orchestrates AI |
| User context via tool parameters | userId passed to MCP tools — language-agnostic |
| Defense in depth | 7 auth layers (JWT → API key → subscription → ownership) |
| Graceful degradation | Agent Farm down → Claude fallback. Kite down → CSV import |
| Full transparency | Every AI analysis saved with model, prompt, raw response |
| FIFO compliance | Tax engine uses legally required lot matching |
| Separation of concerns | Java for domain, Node.js for AI orchestration, React for UI |

---

## 18. Environment Variables

### Portfolio Tracker API
| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_HOST` | Yes | `localhost` | MySQL host |
| `DB_PORT` | Yes | `3306` | MySQL port |
| `DB_NAME` | Yes | `portfolio_tracker` | Database name |
| `DB_USER` | Yes | `root` | MySQL user |
| `DB_PASSWORD` | Yes | — | MySQL password |
| `JWT_SECRET` | Yes | — | JWT signing key (min 32 chars) |
| `ANTHROPIC_API_KEY` | Yes | — | Claude API key |
| `ANTHROPIC_MODEL` | No | `claude-sonnet-4-6` | Claude model |
| `AGENT_FARM_URL` | No | `http://localhost:8082` | Agent Farm URL |
| `AGENT_FARM_API_KEY` | No | `admin-secret` | Agent Farm auth |
| `KNOWLEDGE_STORE_URL` | No | `http://localhost:3010` | Knowledge Store service URL |
| `GOOGLE_CLIENT_ID` | No | — | Google OAuth client ID |
| `KITE_API_KEY` | No | — | Zerodha API key |
| `KITE_API_SECRET` | No | — | Zerodha API secret |
| `FRONTEND_URL` | Yes | `http://localhost:5173` | CORS origin |

### MCP Farm (Gateway + Agent Farm)
| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DATABASE_URL` | Yes | — | PostgreSQL connection |
| `REDIS_URL` | Yes | — | Redis connection |
| `ADMIN_API_KEY` | Yes | `admin-secret` | Admin authentication |
| `ANTHROPIC_API_KEY` | Yes | — | Claude API key |
| `OPENAI_API_KEY` | No | — | OpenAI fallback |

### Knowledge Store
| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DATABASE_URL` | Yes | — | PostgreSQL connection (uses mcp_farm DB) |
| `OLLAMA_URL` | No | `http://127.0.0.1:11434` | Ollama embedding endpoint |
| `PORT` | No | `3010` | Server port |
