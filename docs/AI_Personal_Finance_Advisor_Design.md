# AI-Powered Personal Finance Advisor — Complete Solution Design

**Document Version:** 1.0
**Date:** April 5, 2026
**Author:** Claude (Architect) + Markandey Singh
**Status:** Design Complete — Ready for Implementation

---

## Table of Contents

1. [Executive Summary](#1-executive-summary)
2. [Gap Analysis: Current vs. Target State](#2-gap-analysis)
3. [Regulatory Strategy](#3-regulatory-strategy)
4. [Feature Roadmap](#4-feature-roadmap)
5. [Architecture Design](#5-architecture-design)
6. [Database Schema Additions](#6-database-schema-additions)
7. [New API Endpoints](#7-new-api-endpoints)
8. [Frontend Additions](#8-frontend-additions)
9. [MCP & Agent Farm Integration](#9-mcp--agent-farm-integration)
10. [Local vs. Cloud Model Routing](#10-local-vs-cloud-model-routing)
11. [Agent-Executable Implementation Tasks](#11-agent-executable-implementation-tasks)
12. [Market Positioning](#12-market-positioning)
13. [Cost Model](#13-cost-model)
14. [Risk Register](#14-risk-register)

---

## 1. Executive Summary

### Vision

Transform the existing **Portfolio Tracker** application into a full-fledged **AI-Powered Personal Finance Advisor** that provides Indian retail investors with intelligent, context-aware financial guidance — starting as a personal tool for Markandey, evolving into a production SaaS product.

### What Exists Today

The portfolio-tracker is already a production-grade system with:

- Multi-portfolio management with CRUD, holdings, transactions
- Zerodha Kite OAuth integration (holdings sync every 15 minutes, live prices)
- Claude-powered AI analysis (health score, risk profile, stock recommendations, rebalancing)
- SSE streaming chat with web search (via Agent Farm)
- Indian tax calculation engine (LTCG 12.5%, STCG 20%, FIFO lot matching, grandfathering)
- Tradebook & income tracking (Zerodha CSV import, FD/dividend/SGB/ESOP/rental income)
- Approval workflow system (PENDING → APPROVED → EXECUTED flow)
- Analysis history with full transparency (prompts, raw responses, parsed results)
- Real-time WebSocket price updates via STOMP
- Technical analysis (SMA50, SMA200, RSI, MACD)
- Dark-themed React SPA with responsive mobile-first design

### What's Missing for a True AI Finance Advisor

The current system is a **portfolio tracker with AI bolted on**. To become an **AI-first personal finance advisor**, it needs:

1. **Goal-Based Planning** — No concept of financial goals (retirement, house, education, emergency fund)
2. **Proactive Intelligence** — AI only responds when asked; no alerts, no daily briefings, no event-driven nudges
3. **Tax Optimization Engine** — Tax calculation exists but no proactive tax-loss harvesting suggestions, no "what-if" scenarios
4. **Performance Analytics** — No XIRR calculation, no benchmark comparison, no time-weighted returns
5. **Market Intelligence Pipeline** — No news aggregation, no earnings tracking, no sector analysis beyond static sector tags
6. **Mutual Fund Support** — No MF tracking despite Kite API having MF endpoints
7. **Automated Execution** — Approval system exists but no Kite order placement integration
8. **Risk Profiling** — No structured risk assessment questionnaire or risk score
9. **Multi-Asset View** — Only equities; no FDs, gold, PPF, EPF, NPS, real estate in net worth
10. **Conversation Memory** — Chat history is per-portfolio, no cross-session context or user preference learning

---

## 2. Gap Analysis

### Feature Matrix: Current State vs. Target

| Feature Area | Current State | Personal Use Target | Production Target |
|---|---|---|---|
| **Portfolio Tracking** | ✅ Multi-portfolio, holdings, transactions | ✅ Same | ✅ Same |
| **Kite Integration** | ✅ OAuth, sync, live prices | ✅ + GTT orders, MF holdings | ✅ + order placement via approval |
| **AI Analysis** | ✅ Health score, risks, opportunities | ✅ + daily briefing, proactive alerts | ✅ + personalized insights at scale |
| **AI Chat** | ✅ SSE streaming, web search | ✅ + persistent memory, context | ✅ + rate limiting, usage tracking |
| **Rebalancing** | ✅ Suggestions only | ✅ + one-click execution via GTT | ✅ + auto-rebalance with approval |
| **Tax Engine** | ✅ LTCG/STCG, FIFO, grandfathering | ✅ + harvesting suggestions, what-if | ✅ + advance tax estimation |
| **Income Tracking** | ✅ FD, dividend, SGB, ESOP, rental | ✅ + auto-detect from Kite dividends | ✅ + P&L statement generation |
| **Performance** | ❌ None | ✅ XIRR, benchmark comparison | ✅ + time-weighted returns, alpha |
| **Goals** | ❌ None | ✅ Basic goal tracking | ✅ + SIP recommendations per goal |
| **Risk Profile** | ❌ None | ✅ Questionnaire + score | ✅ + dynamic adjustment |
| **Market Intelligence** | ❌ None | ✅ News + earnings calendar | ✅ + sector rotation signals |
| **Mutual Funds** | ❌ None | ✅ Holdings from Kite | ✅ + SIP tracking, NAV history |
| **Net Worth** | ❌ None | ✅ Multi-asset dashboard | ✅ + trend analysis |
| **Notifications** | ❌ None | ✅ In-app + email | ✅ + push, scheduled digests |
| **Execution** | ❌ None | ⚠️ Manual via Kite link | ✅ GTT via API with approval |

### Entity Relationship: What Exists

```
User (1) ──→ (N) Portfolio (1) ──→ (N) PortfolioHolding ──→ Stock
                           (1) ──→ (N) Transaction ──→ Stock
User (1) ──→ (N) IncomeEntry
User (1) ──→ (N) TradebookEntry
User (1) ──→ (N) ChatHistory
User (1) ──→ (N) AnalysisHistory
User (1) ──→ (N) ApprovalRequest
User (1) ──→ (N) Alert ──→ Stock/Portfolio
Stock (1) ──→ (N) StockPriceHistory
```

### Entity Relationship: What Needs to Be Added

```
User (1) ──→ (1) RiskProfile
User (1) ──→ (1) UserPreference
User (1) ──→ (N) FinancialGoal ──→ (N) GoalAllocation
User (1) ──→ (N) NetWorthAsset
User (1) ──→ (N) MutualFundHolding ──→ MutualFund
User (1) ──→ (N) Watchlist ──→ (N) WatchlistItem ──→ Stock
User (1) ──→ (N) Notification
User (1) ──→ (N) DailyBriefing
User (1) ──→ (N) TaxHarvestOpportunity
Portfolio (1) ──→ (N) PerformanceSnapshot (daily/weekly XIRR, benchmark delta)
Stock (1) ──→ (N) NewsArticle (via embeddings, not FK)
```

---

## 3. Regulatory Strategy

### Phase 1: Personal Use — No Registration Required

For personal use, no SEBI registration is needed. You're building a tool for yourself.

### Phase 2: Production — Two-Track Approach

**Track A: Information Platform (No RIA Required)**

Position the product as an **educational and analytical tool**, not an investment advisor:

- Display AI-generated analysis clearly labeled as "AI-generated insights, not investment advice"
- No specific BUY/SELL/HOLD recommendations tied to user's portfolio (reframe as "analysis")
- General market commentary, educational content, portfolio analytics
- Tax calculation is informational — this is fine without registration
- Performance tracking is analytical — this is fine

**Track B: Full Advisory (SEBI RIA Registration)**

If you want to provide personalized buy/sell recommendations:

- Apply for SEBI RIA registration under individual category (up to 300 clients)
- Requires NISM X-A and X-B certifications
- Financial deposit: ₹1 lakh minimum
- Fee-only model (no commissions) — aligns perfectly with subscription pricing
- AI usage must be disclosed to clients (transparency panel already exists!)
- Quarterly reporting to SEBI on AI/ML usage

**Recommended Path:** Launch as Track A (information platform) to validate product-market fit, then apply for RIA registration once you hit ~50 paying users. The existing transparency panel (showing data sent to AI and raw responses) is a regulatory asset — most competitors don't have this.

### Compliance Features Already Built

- ✅ Analysis transparency panel (data sent to AI, raw response visible)
- ✅ SEBI disclaimer in the UI
- ✅ History of all AI interactions stored (audit trail)
- ✅ Approval workflow for trade execution

### Compliance Features to Add

- [ ] Risk profiling questionnaire (SEBI mandates suitability assessment)
- [ ] Disclaimer on every AI-generated response: "This is AI-generated analysis, not personalized investment advice"
- [ ] User consent flow for AI data processing
- [ ] Data retention and deletion policy (account deletion with full data purge)
- [ ] Terms of service + privacy policy pages

---

## 4. Feature Roadmap

### Phase 1: Personal Use Enhancement (4-6 weeks)

**Priority: Immediate value for daily use**

| # | Feature | Effort | Value |
|---|---------|--------|-------|
| 1.1 | XIRR & Performance Analytics | 1 week | Critical — you need to know actual returns |
| 1.2 | Daily AI Briefing (scheduled) | 1 week | High — replaces morning routine |
| 1.3 | Tax-Loss Harvesting Suggestions | 1 week | High — saves real money |
| 1.4 | Watchlist with Price Alerts | 3 days | Medium — track stocks of interest |
| 1.5 | Risk Profile Questionnaire | 2 days | Medium — enables personalized AI advice |
| 1.6 | Enhanced AI Chat with Memory | 1 week | High — chat should remember preferences |
| 1.7 | Mutual Fund Holdings (Kite MF API) | 3 days | Medium — complete portfolio picture |

### Phase 2: Net Worth & Goals (3-4 weeks)

| # | Feature | Effort | Value |
|---|---------|--------|-------|
| 2.1 | Net Worth Dashboard (multi-asset) | 1 week | High — holistic financial view |
| 2.2 | Financial Goal Planner | 1 week | High — goal-based investing |
| 2.3 | Benchmark Comparison (Nifty 50, Midcap) | 3 days | Medium — alpha measurement |
| 2.4 | Market Intelligence Pipeline (news + embeddings) | 1 week | Medium — informed decisions |
| 2.5 | Advance Tax Estimation | 3 days | Medium — quarterly tax planning |

### Phase 3: Production Readiness (4-6 weeks)

| # | Feature | Effort | Value |
|---|---------|--------|-------|
| 3.1 | Multi-tenancy hardening | 1 week | Critical — data isolation |
| 3.2 | Rate limiting & usage tracking | 3 days | Critical — cost control |
| 3.3 | Notification system (in-app + email) | 1 week | High — engagement |
| 3.4 | GTT Order Placement via Kite API | 1 week | High — one-click execution |
| 3.5 | Onboarding flow + risk assessment | 3 days | Critical — first impressions |
| 3.6 | Subscription & billing | 1 week | Critical — revenue |
| 3.7 | Landing page & marketing site | 3 days | Critical — user acquisition |
| 3.8 | Mobile responsiveness polish | 3 days | High — most users on mobile |

### Phase 4: Intelligence & Scale (ongoing)

| # | Feature | Effort | Value |
|---|---------|--------|-------|
| 4.1 | RAG with news embeddings (pgvector) | 2 weeks | High — contextual AI |
| 4.2 | Automated rebalancing with approval | 1 week | Medium — automation |
| 4.3 | Sector rotation signals | 1 week | Medium — alpha generation |
| 4.4 | Portfolio simulation / backtesting | 2 weeks | Medium — "what if" scenarios |
| 4.5 | Social features (anonymized portfolio comparison) | 2 weeks | Low — growth lever |

---

## 5. Architecture Design

### System Architecture (Target State)

```
┌──────────────────────────────────────────────────────────────────┐
│                         FRONTEND (React SPA)                      │
│  Dashboard │ Holdings │ AI Chat │ Goals │ Tax │ NetWorth │ Market │
└──────────┬───────────────────────────────────┬───────────────────┘
           │ REST + SSE + WebSocket             │
           ▼                                    │
┌──────────────────────────────────────────────────────────────────┐
│                    PORTFOLIO TRACKER API (Spring Boot)             │
│                                                                    │
│  ┌─────────────┐ ┌──────────────┐ ┌─────────────┐ ┌───────────┐ │
│  │ Portfolio    │ │ AI Advisory  │ │ Tax Engine  │ │ Goals     │ │
│  │ Service     │ │ Service      │ │ Service     │ │ Service   │ │
│  └─────────────┘ └──────┬───────┘ └─────────────┘ └───────────┘ │
│  ┌─────────────┐ ┌──────┼───────┐ ┌─────────────┐ ┌───────────┐ │
│  │ Performance │ │ Market│Intel │ │ NetWorth    │ │ Notifi-   │ │
│  │ Service     │ │ Service      │ │ Service     │ │ cation    │ │
│  └─────────────┘ └──────┼───────┘ └─────────────┘ └───────────┘ │
│                         │                                         │
│  ┌──────────────────────┼────────────────────────────────────┐   │
│  │           Scheduler (Spring @Scheduled)                    │   │
│  │  • Daily briefing (6 AM)                                   │   │
│  │  • Tax harvesting scan (weekly Sunday)                     │   │
│  │  • Performance snapshot (daily market close)               │   │
│  │  • Holdings sync (every 15 min — existing)                 │   │
│  │  • News aggregation (every 4 hours)                        │   │
│  │  • Price alerts check (every 5 min during market hours)    │   │
│  └───────────────────────────────────────────────────────────┘   │
└──────────────────────┬───────────────────────────────────────────┘
                       │
         ┌─────────────┼──────────────┐
         ▼             ▼              ▼
   ┌──────────┐  ┌──────────┐  ┌──────────────┐
   │ MySQL 8  │  │ Kite API │  │ MCP Farm     │
   │          │  │ (Zerodha)│  │              │
   │ All app  │  │ Holdings │  │ Agent Farm   │
   │ data     │  │ Prices   │  │ (LLM Router) │
   │          │  │ GTT      │  │              │
   │          │  │ MF       │  │ News MCP     │
   └──────────┘  └──────────┘  │ Market MCP   │
                               │ Tax MCP      │
                               └──────────────┘
                                      │
                          ┌───────────┼───────────┐
                          ▼           ▼           ▼
                    ┌──────────┐ ┌────────┐ ┌─────────┐
                    │ Claude   │ │ OpenAI │ │ Ollama  │
                    │ (Complex)│ │ (Med)  │ │ (Local) │
                    └──────────┘ └────────┘ └─────────┘
```

### Key Architecture Decisions

**1. Stay in the existing Spring Boot app** — Don't create a separate microservice. The portfolio-tracker is already well-structured with clean service separation. Adding features as new services/controllers within the same app avoids the operational overhead of another deployment.

**2. Use Spring @Scheduled for background jobs** — AsyncConfig already exists with a thread pool. Scheduled tasks for daily briefings, tax scans, and performance snapshots are simpler than setting up a separate job scheduler.

**3. Route AI tasks through Agent Farm** — The MCP Farm + Agent Farm infrastructure is already built. Complex tasks (rebalancing, deep analysis) go to Claude via Agent Farm. Simple tasks (price monitoring, formatting) use local Ollama models.

**4. pgvector for news embeddings** — Add pgvector to the existing PostgreSQL (used by MCP Farm). News articles get embedded and stored for RAG-based market intelligence in AI responses.

**5. Keep MySQL for application data** — All new entities go in the existing MySQL database. No need for a second relational database.

---

## 6. Database Schema Additions

### New Entities

#### 6.1 UserPreference

```sql
CREATE TABLE user_preferences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    risk_score INT,                          -- 1-10, from questionnaire
    risk_profile VARCHAR(20),                -- CONSERVATIVE, MODERATE, AGGRESSIVE
    investment_horizon VARCHAR(20),          -- SHORT (< 3y), MEDIUM (3-7y), LONG (> 7y)
    preferred_sectors JSON,                  -- ["IT", "Banking", "Pharma"]
    tax_slab VARCHAR(20),                    -- "0", "5", "20", "30" (percentage)
    annual_income_range VARCHAR(20),         -- "0-5L", "5-10L", "10-20L", "20-50L", "50L+"
    daily_briefing_enabled BOOLEAN DEFAULT TRUE,
    briefing_time TIME DEFAULT '06:00:00',
    email_notifications BOOLEAN DEFAULT TRUE,
    questionnaire_completed_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);
```

#### 6.2 FinancialGoal

```sql
CREATE TABLE financial_goals (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL,             -- "Retirement", "House Down Payment"
    goal_type VARCHAR(30) NOT NULL,         -- RETIREMENT, HOUSE, EDUCATION, EMERGENCY, WEALTH, CUSTOM
    target_amount DECIMAL(20,2) NOT NULL,
    current_amount DECIMAL(20,2) DEFAULT 0,
    target_date DATE NOT NULL,
    monthly_sip_needed DECIMAL(20,2),       -- calculated
    expected_return_rate DECIMAL(5,2),      -- annual %, e.g. 12.00
    inflation_rate DECIMAL(5,2) DEFAULT 6.00,
    priority INT DEFAULT 1,                 -- 1 = highest
    status VARCHAR(20) DEFAULT 'ACTIVE',    -- ACTIVE, ACHIEVED, PAUSED, ABANDONED
    notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_status (user_id, status)
);
```

#### 6.3 GoalAllocation

```sql
CREATE TABLE goal_allocations (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    goal_id BIGINT NOT NULL,
    portfolio_id BIGINT,                    -- linked portfolio
    asset_type VARCHAR(30) NOT NULL,        -- EQUITY, MUTUAL_FUND, FD, PPF, NPS, GOLD, OTHER
    allocated_amount DECIMAL(20,2) NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (goal_id) REFERENCES financial_goals(id),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    INDEX idx_goal (goal_id)
);
```

#### 6.4 NetWorthAsset

```sql
CREATE TABLE net_worth_assets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    asset_type VARCHAR(30) NOT NULL,        -- EQUITY, MUTUAL_FUND, FD, PPF, EPF, NPS, GOLD, REAL_ESTATE, SAVINGS, CRYPTO, OTHER
    name VARCHAR(200) NOT NULL,             -- "HDFC Bank FD", "PPF Account", "Flat in Bangalore"
    current_value DECIMAL(20,2) NOT NULL,
    invested_value DECIMAL(20,2),
    institution VARCHAR(100),               -- "HDFC Bank", "SBI", "Zerodha"
    maturity_date DATE,
    interest_rate DECIMAL(5,2),
    is_liquid BOOLEAN DEFAULT TRUE,
    notes TEXT,
    last_updated DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_type (user_id, asset_type)
);
```

#### 6.5 MutualFundHolding

```sql
CREATE TABLE mutual_fund_holdings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    portfolio_id BIGINT,
    scheme_name VARCHAR(300) NOT NULL,
    scheme_code VARCHAR(50),                -- AMFI code
    isin VARCHAR(20),
    folio_number VARCHAR(50),
    units DECIMAL(20,4) NOT NULL,
    average_nav DECIMAL(20,4),
    current_nav DECIMAL(20,4),
    invested_value DECIMAL(20,2),
    current_value DECIMAL(20,2),
    fund_type VARCHAR(30),                  -- EQUITY, DEBT, HYBRID, INDEX, ELSS, LIQUID
    amc VARCHAR(100),                       -- Asset Management Company
    last_synced_at TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    INDEX idx_user (user_id),
    INDEX idx_scheme (scheme_code)
);
```

#### 6.6 PerformanceSnapshot

```sql
CREATE TABLE performance_snapshots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    portfolio_id BIGINT NOT NULL,
    snapshot_date DATE NOT NULL,
    total_value DECIMAL(20,2) NOT NULL,
    total_invested DECIMAL(20,2) NOT NULL,
    day_change DECIMAL(20,2),
    day_change_pct DECIMAL(8,4),
    absolute_return_pct DECIMAL(10,4),
    xirr DECIMAL(10,4),                    -- annualized XIRR
    nifty50_value DECIMAL(20,4),           -- benchmark on same date
    nifty50_return_pct DECIMAL(10,4),      -- benchmark return from first investment
    alpha DECIMAL(10,4),                   -- xirr - benchmark return
    holdings_count INT,
    sector_allocation JSON,                -- snapshot of allocation
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    UNIQUE KEY uk_portfolio_date (portfolio_id, snapshot_date),
    INDEX idx_user_date (user_id, snapshot_date)
);
```

#### 6.7 Watchlist & WatchlistItem

```sql
CREATE TABLE watchlists (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    name VARCHAR(100) NOT NULL DEFAULT 'Default',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user (user_id)
);

CREATE TABLE watchlist_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    watchlist_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    target_buy_price DECIMAL(20,2),
    target_sell_price DECIMAL(20,2),
    notes VARCHAR(500),
    added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (watchlist_id) REFERENCES watchlists(id),
    FOREIGN KEY (stock_id) REFERENCES stocks(id),
    UNIQUE KEY uk_watchlist_stock (watchlist_id, stock_id)
);
```

#### 6.8 Notification

```sql
CREATE TABLE notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    type VARCHAR(30) NOT NULL,              -- PRICE_ALERT, TAX_HARVEST, DAILY_BRIEFING, REBALANCE, GOAL_MILESTONE, SYSTEM
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    action_url VARCHAR(500),                -- deep link within app
    is_read BOOLEAN DEFAULT FALSE,
    priority VARCHAR(10) DEFAULT 'NORMAL',  -- LOW, NORMAL, HIGH, URGENT
    metadata JSON,                          -- flexible payload
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    INDEX idx_user_unread (user_id, is_read),
    INDEX idx_user_type (user_id, type)
);
```

#### 6.9 TaxHarvestOpportunity

```sql
CREATE TABLE tax_harvest_opportunities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    portfolio_id BIGINT NOT NULL,
    stock_id BIGINT NOT NULL,
    holding_id BIGINT NOT NULL,
    unrealized_loss DECIMAL(20,2) NOT NULL,
    current_price DECIMAL(20,2) NOT NULL,
    average_buy_price DECIMAL(20,2) NOT NULL,
    quantity INT NOT NULL,
    holding_days INT NOT NULL,
    gain_type VARCHAR(10) NOT NULL,         -- STCG or LTCG
    potential_tax_saving DECIMAL(20,2) NOT NULL,
    suggested_action TEXT,                  -- "Sell and rebuy after market close" or "Sell and buy similar index fund"
    status VARCHAR(20) DEFAULT 'ACTIVE',    -- ACTIVE, EXECUTED, EXPIRED, DISMISSED
    identified_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP,                   -- opportunity may expire with price changes
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(id),
    FOREIGN KEY (stock_id) REFERENCES stocks(id),
    INDEX idx_user_status (user_id, status)
);
```

#### 6.10 DailyBriefing

```sql
CREATE TABLE daily_briefings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    briefing_date DATE NOT NULL,
    portfolio_summary TEXT NOT NULL,         -- "Your portfolio is up ₹12,500 (0.8%)"
    market_summary TEXT,                    -- "Nifty 50 closed at 24,150 (+0.5%)"
    top_movers TEXT,                        -- "Top gainer: RELIANCE +3.2%, Top loser: INFY -1.8%"
    tax_alerts TEXT,                        -- "3 holdings approaching LTCG threshold"
    ai_insights TEXT,                       -- "Consider rebalancing IT sector (28% → 20%)"
    goal_updates TEXT,                      -- "Retirement goal is 45% funded, on track"
    full_briefing_html TEXT,                -- rendered HTML for email
    generated_by VARCHAR(50),               -- model used
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id),
    UNIQUE KEY uk_user_date (user_id, briefing_date),
    INDEX idx_date (briefing_date)
);
```

### Modifications to Existing Tables

#### Users Table — Add Fields

```sql
ALTER TABLE users ADD COLUMN phone VARCHAR(15);
ALTER TABLE users ADD COLUMN pan_number VARCHAR(10);           -- for tax features
ALTER TABLE users ADD COLUMN onboarding_completed BOOLEAN DEFAULT FALSE;
ALTER TABLE users ADD COLUMN subscription_tier VARCHAR(20) DEFAULT 'FREE';  -- FREE, PRO, PREMIUM
ALTER TABLE users ADD COLUMN subscription_expires_at TIMESTAMP;
```

#### Alert Table — Enhance

```sql
ALTER TABLE alerts ADD COLUMN alert_category VARCHAR(30);      -- PRICE, TAX, REBALANCE, GOAL
ALTER TABLE alerts ADD COLUMN notification_channel VARCHAR(20) DEFAULT 'IN_APP'; -- IN_APP, EMAIL, BOTH
ALTER TABLE alerts ADD COLUMN message_template TEXT;
ALTER TABLE alerts ADD COLUMN cooldown_hours INT DEFAULT 24;    -- don't re-trigger within N hours
```

---

## 7. New API Endpoints

### 7.1 Performance Analytics (`/api/performance`)

```
GET  /api/performance/portfolio/{id}/xirr                    → { xirr, cagr, absoluteReturn, period }
GET  /api/performance/portfolio/{id}/benchmark                → { portfolioReturn, nifty50Return, alpha, period }
GET  /api/performance/portfolio/{id}/history?period=1Y        → List<PerformanceSnapshot>
GET  /api/performance/portfolio/{id}/day-change               → { dayChange, dayChangePct, topMovers }
GET  /api/performance/portfolio/{id}/sector-analysis           → { sectorWeights, sectorReturns, recommendations }
```

### 7.2 Financial Goals (`/api/goals`)

```
GET    /api/goals                                             → List<FinancialGoalResponse>
POST   /api/goals                                             → FinancialGoalResponse
PUT    /api/goals/{id}                                        → FinancialGoalResponse
DELETE /api/goals/{id}                                        → 204
POST   /api/goals/{id}/allocations                            → GoalAllocation
GET    /api/goals/{id}/projections                            → { onTrack, monthlyNeeded, projectedDate, scenarios[] }
GET    /api/goals/summary                                     → { totalGoals, onTrack, needsAttention, totalTarget, totalCurrent }
```

### 7.3 Net Worth (`/api/networth`)

```
GET    /api/networth                                          → { totalNetWorth, assets[], liabilities[], breakdown }
POST   /api/networth/assets                                   → NetWorthAssetResponse
PUT    /api/networth/assets/{id}                              → NetWorthAssetResponse
DELETE /api/networth/assets/{id}                              → 204
GET    /api/networth/history?period=1Y                        → List<NetWorthSnapshot>
GET    /api/networth/allocation                               → { byType, byLiquidity, byRisk }
```

### 7.4 Watchlists (`/api/watchlists`)

```
GET    /api/watchlists                                        → List<WatchlistResponse>
POST   /api/watchlists                                        → WatchlistResponse
POST   /api/watchlists/{id}/items                             → WatchlistItemResponse
DELETE /api/watchlists/{watchlistId}/items/{itemId}           → 204
```

### 7.5 Notifications (`/api/notifications`)

```
GET    /api/notifications?unread=true                         → List<NotificationResponse>
POST   /api/notifications/{id}/read                           → 200
POST   /api/notifications/read-all                            → 200
GET    /api/notifications/count                               → { unread: 5 }
```

### 7.6 Risk Profile (`/api/profile`)

```
GET    /api/profile/preferences                               → UserPreferenceResponse
PUT    /api/profile/preferences                               → UserPreferenceResponse
POST   /api/profile/risk-assessment                           → { riskScore, riskProfile, recommendations }
GET    /api/profile/risk-questions                             → List<RiskQuestion>  (questionnaire)
```

### 7.7 Tax Optimization (additions to existing `/api/tax`)

```
GET    /api/tax/harvest-opportunities?portfolioId=1           → List<TaxHarvestOpportunity>
POST   /api/tax/harvest-opportunities/{id}/dismiss            → 200
GET    /api/tax/what-if?action=SELL&stockId=5&quantity=10     → { taxImpact, netProceeds, recommendation }
GET    /api/tax/advance-estimate?financialYear=2025-26        → { estimatedTax, quarterlySchedule }
```

### 7.8 Daily Briefing (addition to existing `/api/ai`)

```
GET    /api/ai/briefing/today                                 → DailyBriefingResponse
GET    /api/ai/briefing/history?limit=7                       → List<DailyBriefingSummary>
POST   /api/ai/briefing/generate                              → DailyBriefingResponse (force regenerate)
```

### 7.9 Mutual Funds (addition to existing `/api/kite`)

```
GET    /api/kite/mf-holdings                                  → List<MutualFundHoldingResponse>
POST   /api/kite/mf-sync                                      → { synced, total }
```

### 7.10 Enhanced AI Endpoints (additions to `/api/ai`)

```
POST   /api/ai/portfolio/{id}/tax-advice                      → SSE (streaming tax optimization advice)
POST   /api/ai/portfolio/{id}/goal-review                     → SSE (streaming goal progress + recommendations)
POST   /api/ai/market-analysis                                → SSE (streaming market outlook with web search)
```

---

## 8. Frontend Additions

### 8.1 New Pages

| Page | Route | Description |
|------|-------|-------------|
| Performance | `/performance` | XIRR charts, benchmark comparison, daily P&L, alpha tracking |
| Goals | `/goals` | Financial goal cards, progress bars, SIP calculator, projections |
| Net Worth | `/networth` | Multi-asset treemap, allocation donut, trend line |
| Watchlist | `/watchlist` | Stock watchlist with price alerts, AI recommendations |
| Market | `/market` | Market overview, sector heatmap, news feed (Phase 2) |
| Notifications | — (slide-over panel) | Bell icon in header, notification list |
| Onboarding | `/onboarding` | Risk questionnaire, Kite connect, portfolio import (Phase 3) |
| Settings | `/settings` | Preferences, notification settings, briefing time, subscription |

### 8.2 New Components

**Performance:**
- `PerformanceChart.tsx` — Line chart with portfolio value vs Nifty 50 over time (Recharts)
- `XIRRCard.tsx` — XIRR, CAGR, absolute return display with period selector
- `DailyPnLCard.tsx` — Today's change with top movers
- `SectorPerformance.tsx` — Sector-wise return breakdown

**Goals:**
- `GoalCard.tsx` — Goal with progress bar, target amount, monthly SIP needed
- `GoalForm.tsx` — Create/edit goal modal
- `GoalProjection.tsx` — Chart showing projected growth with scenarios (optimistic/expected/pessimistic)
- `SIPCalculator.tsx` — Interactive SIP calculator

**Net Worth:**
- `NetWorthSummary.tsx` — Total net worth with month-over-month change
- `AssetAllocationChart.tsx` — Donut chart by asset type
- `AssetForm.tsx` — Add/edit asset modal
- `LiquidityBreakdown.tsx` — Liquid vs illiquid assets

**AI Enhancement:**
- `DailyBriefingCard.tsx` — Morning briefing with portfolio + market summary
- `TaxHarvestAlert.tsx` — Banner/card showing tax saving opportunities
- `RiskQuestionnaire.tsx` — Multi-step risk assessment form
- `AIMemoryIndicator.tsx` — Shows what the AI remembers about user preferences

**Common:**
- `NotificationBell.tsx` — Header icon with unread count badge
- `NotificationPanel.tsx` — Slide-over panel listing notifications
- `BenchmarkSelector.tsx` — Dropdown to select benchmark (Nifty 50, Nifty 500, Midcap 150)
- `CurrencyDisplay.tsx` — Standardized ₹ formatting with lakhs/crores

### 8.3 New Stores (Zustand)

```typescript
// goalStore.ts
interface GoalStore {
  goals: FinancialGoal[];
  setGoals: (goals: FinancialGoal[]) => void;
  addGoal: (goal: FinancialGoal) => void;
  updateGoal: (id: number, updates: Partial<FinancialGoal>) => void;
}

// notificationStore.ts
interface NotificationStore {
  notifications: Notification[];
  unreadCount: number;
  setNotifications: (n: Notification[]) => void;
  markRead: (id: number) => void;
  markAllRead: () => void;
}

// preferenceStore.ts
interface PreferenceStore {
  preferences: UserPreference | null;
  setPreferences: (p: UserPreference) => void;
  riskProfile: string;
}
```

### 8.4 Sidebar Updates

Add new nav items to the existing sidebar:

```
Dashboard (existing)
Holdings (existing)
Performance (NEW — TrendingUp icon)
AI Assessment (existing)
Goals (NEW — Target icon)
Net Worth (NEW — PiggyBank icon)
Watchlist (NEW — Eye icon)
Tax Dashboard (existing)
Income & Trades (existing)
Settings (NEW — Settings icon, bottom)
```

---

## 9. MCP & Agent Farm Integration

### 9.1 New MCP Servers to Build

#### Market Data MCP (`mcp-market-data`)

**Purpose:** Aggregate market data from multiple sources for AI consumption.

**Tools:**
- `get_index_data(index: "NIFTY50" | "NIFTYMIDCAP" | ...)` → Current value, day change, 52w high/low
- `get_sector_performance(period: "1D" | "1W" | "1M")` → Sector-wise returns
- `get_stock_fundamentals(symbol)` → PE, PB, dividend yield, market cap, EPS
- `get_nifty50_constituents()` → Current Nifty 50 stocks with weights
- `get_fii_dii_data(date)` → FII/DII buy/sell data

**Data Sources:** NSE website scraping, Kite historical API, public financial data APIs

#### News Aggregator MCP (`mcp-news-aggregator`)

**Purpose:** Fetch and embed financial news for RAG-based AI responses.

**Tools:**
- `search_news(query, from_date, to_date)` → List of articles with summaries
- `get_stock_news(symbol, limit)` → Recent news for a specific stock
- `get_market_news(category: "EQUITY" | "MF" | "ECONOMY", limit)` → Category news
- `get_earnings_calendar(from_date, to_date)` → Upcoming earnings dates

**Data Sources:** Google News RSS, MoneyControl RSS, Economic Times RSS, BSE/NSE announcements

**Embedding Pipeline:**
- Articles fetched every 4 hours
- Embedded using `nomic-embed-text` on local Ollama
- Stored in pgvector (PostgreSQL) with metadata (source, date, symbols, category)
- 768-dimension vectors for semantic search

#### Tax Calculator MCP (`mcp-tax-calculator`)

**Purpose:** Provide structured tax calculations that AI can use in responses.

**Tools:**
- `calculate_capital_gains(trades: TradePair[])` → LTCG/STCG breakdown
- `estimate_advance_tax(incomes: Income[], gains: CapitalGain[])` → Quarterly schedule
- `find_harvest_opportunities(holdings: Holding[])` → Tax-loss harvesting candidates
- `calculate_what_if(action, holding, quantity)` → Tax impact of a hypothetical trade
- `get_tax_rates(financial_year)` → Current tax slabs and rates

### 9.2 Agent Farm Templates

#### Daily Briefing Agent

```json
{
  "name": "daily-briefing-agent",
  "description": "Generates personalized daily financial briefing",
  "model": "ollama/qwen2.5-coder:32b",
  "systemPrompt": "You are a personal finance briefing assistant for an Indian retail investor. Generate a concise morning briefing covering: 1) Portfolio performance summary 2) Market overview 3) Key stock movers in their portfolio 4) Any tax optimization alerts 5) Goal progress updates. Format for mobile reading. Use ₹ for all amounts.",
  "tools": ["mcp-market-data__get_index_data", "mcp-market-data__get_sector_performance"],
  "maxTokens": 1500,
  "temperature": 0.3
}
```

#### Tax Advisor Agent

```json
{
  "name": "tax-advisor-agent",
  "description": "Provides tax optimization advice for Indian equity investors",
  "model": "anthropic/claude-sonnet-4-6",
  "systemPrompt": "You are a tax advisor specialized in Indian equity taxation. Analyze the user's portfolio and trading history to identify: 1) Tax-loss harvesting opportunities (India has NO wash sale rule) 2) LTCG vs STCG optimization (12.5% vs 20%) 3) Grandfathering benefits for pre-Jan-2018 purchases 4) ₹1.25L LTCG exemption utilization. Always include disclaimers.",
  "tools": ["mcp-tax-calculator__calculate_capital_gains", "mcp-tax-calculator__find_harvest_opportunities"],
  "maxTokens": 2048,
  "temperature": 0.2
}
```

#### Goal Planning Agent

```json
{
  "name": "goal-planning-agent",
  "description": "Helps plan and track financial goals",
  "model": "ollama/qwen2.5-coder:32b",
  "systemPrompt": "You are a financial goal planning assistant. Help Indian retail investors plan for: retirement, house purchase, children's education, emergency fund, wealth building. Consider: inflation (6-7% India), expected equity returns (12-15%), FD rates (6-7%), PPF (7.1%), NPS, ELSS. Recommend asset allocation based on goal timeline and risk profile.",
  "tools": [],
  "maxTokens": 1500,
  "temperature": 0.4
}
```

### 9.3 AI Context Enhancement

The existing AIAnalysisService builds a context string from portfolio data. Enhance it with:

```java
// Current context (already built):
// - Holdings list with prices, sectors, P&L
// - Portfolio value, sector allocation

// NEW context additions:
private String buildEnhancedContext(Long portfolioId, Long userId) {
    StringBuilder ctx = new StringBuilder();

    // Existing portfolio context
    ctx.append(buildPortfolioContext(portfolioId));

    // User risk profile
    UserPreference pref = userPreferenceRepo.findByUserId(userId);
    ctx.append("\n\n## Investor Profile\n");
    ctx.append("Risk Profile: " + pref.getRiskProfile());
    ctx.append(" | Tax Slab: " + pref.getTaxSlab() + "%");
    ctx.append(" | Horizon: " + pref.getInvestmentHorizon());

    // Performance metrics
    PerformanceSnapshot latest = perfRepo.findLatestByPortfolioId(portfolioId);
    ctx.append("\n\n## Performance\n");
    ctx.append("XIRR: " + latest.getXirr() + "% | ");
    ctx.append("Nifty50 Return: " + latest.getNifty50ReturnPct() + "% | ");
    ctx.append("Alpha: " + latest.getAlpha() + "%");

    // Goals summary
    List<FinancialGoal> goals = goalRepo.findActiveByUserId(userId);
    ctx.append("\n\n## Financial Goals\n");
    for (FinancialGoal g : goals) {
        ctx.append("- " + g.getName() + ": ₹" + g.getCurrentAmount() + "/" + g.getTargetAmount());
        ctx.append(" (Target: " + g.getTargetDate() + ")\n");
    }

    // Tax situation
    TaxSummaryResponse tax = taxService.calculateTax(currentFY());
    ctx.append("\n\n## Tax Situation (Current FY)\n");
    ctx.append("LTCG: ₹" + tax.getEquityLtcgGains() + " | ");
    ctx.append("STCG: ₹" + tax.getEquityStcgGains() + " | ");
    ctx.append("Tax Payable: ₹" + tax.getTotalCapitalGainsTax());

    return ctx.toString();
}
```

---

## 10. Local vs. Cloud Model Routing

### Task Classification Matrix

| Task | Model | Reason | Est. Cost/Call |
|------|-------|--------|----------------|
| **Daily Briefing** | Ollama (Qwen 32B) | Templated, runs daily, predictable format | $0.00 |
| **Price Alert Formatting** | Ollama (Qwen 32B) | Simple text generation | $0.00 |
| **Goal Projection Calc** | None (pure math) | SIP formula, no LLM needed | $0.00 |
| **XIRR Calculation** | None (pure math) | Newton-Raphson method in Java | $0.00 |
| **Tax Calculation** | None (rule engine) | Deterministic rules | $0.00 |
| **News Embedding** | Ollama (nomic-embed-text) | Batch embedding, runs every 4h | $0.00 |
| **Portfolio Chat (simple)** | Ollama (Qwen 32B) | "What's my IT allocation?" type questions | $0.00 |
| **Portfolio Chat (complex)** | Claude Sonnet | "Should I rebalance given the upcoming election?" | ~$0.03 |
| **Portfolio Analysis** | Claude Sonnet | Complex multi-factor analysis | ~$0.05 |
| **Rebalancing Suggestions** | Claude Sonnet | Requires deep reasoning about correlations | ~$0.05 |
| **Tax Optimization Advice** | Claude Sonnet | Complex tax law + portfolio interaction | ~$0.04 |
| **Stock Deep Dive** | Claude Sonnet + Web Search | Needs current information | ~$0.08 |
| **Market Outlook** | Claude Sonnet + Web Search | Needs current macro data | ~$0.08 |

### Routing Logic

```java
public enum TaskComplexity {
    LOCAL,      // Ollama — free, fast, good for templated tasks
    CLOUD,      // Claude Sonnet — complex reasoning
    CLOUD_PLUS  // Claude Sonnet + web search — needs current data
}

public TaskComplexity classifyTask(String userMessage, String taskType) {
    // Rule-based classification
    if (taskType.equals("DAILY_BRIEFING") || taskType.equals("PRICE_ALERT")) {
        return TaskComplexity.LOCAL;
    }
    if (taskType.equals("ANALYSIS") || taskType.equals("REBALANCING") || taskType.equals("TAX_ADVICE")) {
        return TaskComplexity.CLOUD;
    }

    // Chat messages — classify by content
    String lower = userMessage.toLowerCase();

    // Needs current info → cloud + web search
    if (lower.contains("news") || lower.contains("latest") || lower.contains("today")
        || lower.contains("market") || lower.contains("budget") || lower.contains("rbi")) {
        return TaskComplexity.CLOUD_PLUS;
    }

    // Simple lookups → local
    if (lower.matches(".*(what is|how much|show me|list|my holdings|my portfolio|allocation).*")) {
        return TaskComplexity.LOCAL;
    }

    // Complex reasoning → cloud
    if (lower.matches(".*(should i|recommend|suggest|compare|analyze|rebalance|tax|strategy).*")) {
        return TaskComplexity.CLOUD;
    }

    // Default to cloud for safety
    return TaskComplexity.CLOUD;
}
```

### Estimated Monthly Cost (Personal Use)

| Usage Pattern | Calls/Month | Local | Cloud | Cost |
|---|---|---|---|---|
| Daily briefings | 30 | 30 | 0 | $0 |
| Portfolio analysis | 4 | 0 | 4 | $0.20 |
| Rebalancing | 2 | 0 | 2 | $0.10 |
| Chat (simple) | 60 | 60 | 0 | $0 |
| Chat (complex) | 20 | 0 | 20 | $0.60 |
| Tax advice | 4 | 0 | 4 | $0.16 |
| Market outlook | 8 | 0 | 8 | $0.64 |
| News embeddings | 180 (6/day) | 180 | 0 | $0 |
| **TOTAL** | **308** | **270 (88%)** | **38 (12%)** | **~$1.70/mo** |

For production (per user): $2-5/month depending on usage tier.

---

## 11. Agent-Executable Implementation Tasks

These are structured as self-contained tasks that can be given to a Claude Code agent for implementation. Order matters — earlier tasks are prerequisites for later ones.

### Task Group 1: Foundation (Week 1-2)

---

#### Task 1.1: Add UserPreference Entity & Risk Profile API

**Context:** Create the UserPreference entity, repository, service, controller, and a 10-question risk assessment questionnaire.

**Files to Create:**
- `src/main/java/com/portfolio/entity/UserPreference.java`
- `src/main/java/com/portfolio/repository/UserPreferenceRepository.java`
- `src/main/java/com/portfolio/service/UserPreferenceService.java`
- `src/main/java/com/portfolio/controller/ProfileController.java`
- `src/main/java/com/portfolio/dto/request/RiskAssessmentRequest.java`
- `src/main/java/com/portfolio/dto/response/UserPreferenceResponse.java`
- `src/main/java/com/portfolio/dto/response/RiskQuestionResponse.java`

**Files to Modify:**
- `src/main/java/com/portfolio/entity/User.java` — Add phone, panNumber, onboardingCompleted, subscriptionTier fields

**Schema (create table):** See Section 6.1

**Risk Assessment Algorithm:**
- 10 questions (investment experience, time horizon, loss tolerance, income stability, etc.)
- Each answer maps to 1-10 score
- Average score → Risk Profile: 1-3 CONSERVATIVE, 4-6 MODERATE, 7-10 AGGRESSIVE
- Store in UserPreference with questionnaire_completed_at timestamp

**Acceptance Criteria:**
- `GET /api/profile/risk-questions` returns 10 questions with options
- `POST /api/profile/risk-assessment` accepts answers, returns risk score + profile
- `GET /api/profile/preferences` returns user preferences
- `PUT /api/profile/preferences` updates preferences

---

#### Task 1.2: XIRR & Performance Analytics

**Context:** Implement XIRR calculation using Newton-Raphson method, benchmark comparison against Nifty 50, and daily performance snapshots.

**Files to Create:**
- `src/main/java/com/portfolio/service/PerformanceService.java`
- `src/main/java/com/portfolio/entity/PerformanceSnapshot.java`
- `src/main/java/com/portfolio/repository/PerformanceSnapshotRepository.java`
- `src/main/java/com/portfolio/controller/PerformanceController.java`
- `src/main/java/com/portfolio/dto/response/XIRRResponse.java`
- `src/main/java/com/portfolio/dto/response/BenchmarkComparisonResponse.java`
- `src/main/java/com/portfolio/dto/response/PerformanceSnapshotResponse.java`
- `src/main/java/com/portfolio/service/scheduler/PerformanceScheduler.java`

**XIRR Algorithm:**
- Collect all cash flows: BUY transactions (negative), SELL transactions (positive), current value (positive, today's date)
- Newton-Raphson to find rate r where NPV = 0: `Σ (C_i / (1+r)^((d_i - d_0)/365)) = 0`
- Handle edge cases: all buys no sells (use current value), single transaction, < 1 year of data

**Benchmark Comparison:**
- Fetch Nifty 50 historical data via Kite API (`get_historical_data` for NSE:NIFTY 50`)
- Calculate Nifty 50 return from user's first investment date to today
- Alpha = Portfolio XIRR - Nifty 50 return

**Scheduler:**
- Run daily at 4:30 PM IST (after market close)
- Calculate and store PerformanceSnapshot for each user's portfolio
- Include: total value, invested, XIRR, Nifty 50 value, alpha, sector allocation JSON

**Acceptance Criteria:**
- `GET /api/performance/portfolio/{id}/xirr` returns XIRR, CAGR, absolute return
- `GET /api/performance/portfolio/{id}/benchmark` returns portfolio vs Nifty 50
- `GET /api/performance/portfolio/{id}/history?period=1Y` returns daily snapshots
- Scheduler creates snapshots daily

---

#### Task 1.3: Watchlist with Price Alerts

**Context:** Create watchlist functionality with target price alerts.

**Files to Create:**
- `src/main/java/com/portfolio/entity/Watchlist.java`
- `src/main/java/com/portfolio/entity/WatchlistItem.java`
- `src/main/java/com/portfolio/repository/WatchlistRepository.java`
- `src/main/java/com/portfolio/repository/WatchlistItemRepository.java`
- `src/main/java/com/portfolio/service/WatchlistService.java`
- `src/main/java/com/portfolio/controller/WatchlistController.java`
- `src/main/java/com/portfolio/dto/request/CreateWatchlistRequest.java`
- `src/main/java/com/portfolio/dto/request/AddWatchlistItemRequest.java`
- `src/main/java/com/portfolio/dto/response/WatchlistResponse.java`

**Files to Modify:**
- Existing Alert entity — add alertCategory, notificationChannel, messageTemplate, cooldownHours fields

**Price Alert Logic:**
- During live price updates (existing WebSocket flow), check watchlist target prices
- If currentPrice <= targetBuyPrice or currentPrice >= targetSellPrice, create Notification
- Respect cooldown (don't alert again within N hours)

**Acceptance Criteria:**
- CRUD for watchlists and items
- Price alerts trigger notifications when thresholds are crossed
- Cooldown prevents spam

---

#### Task 1.4: Notification System

**Context:** In-app notification system with WebSocket push.

**Files to Create:**
- `src/main/java/com/portfolio/entity/Notification.java`
- `src/main/java/com/portfolio/repository/NotificationRepository.java`
- `src/main/java/com/portfolio/service/NotificationService.java`
- `src/main/java/com/portfolio/controller/NotificationController.java`
- `src/main/java/com/portfolio/dto/response/NotificationResponse.java`

**Files to Modify:**
- `WebSocketService.java` — Add method to push notifications via STOMP `/topic/notifications/{userId}`

**Notification Types:** PRICE_ALERT, TAX_HARVEST, DAILY_BRIEFING, REBALANCE, GOAL_MILESTONE, SYSTEM

**Acceptance Criteria:**
- `GET /api/notifications` with optional unread filter
- `POST /api/notifications/{id}/read` marks as read
- `GET /api/notifications/count` returns unread count
- WebSocket push on new notification

---

### Task Group 2: AI Intelligence (Week 2-3)

---

#### Task 2.1: Tax-Loss Harvesting Engine

**Context:** Build a scheduled service that scans holdings for tax-loss harvesting opportunities.

**Files to Create:**
- `src/main/java/com/portfolio/entity/TaxHarvestOpportunity.java`
- `src/main/java/com/portfolio/repository/TaxHarvestOpportunityRepository.java`
- `src/main/java/com/portfolio/service/TaxHarvestService.java`
- `src/main/java/com/portfolio/service/scheduler/TaxHarvestScheduler.java`
- `src/main/java/com/portfolio/dto/response/TaxHarvestOpportunityResponse.java`

**Files to Modify:**
- `TaxController.java` — Add harvest opportunity endpoints
- `TaxCalculationService.java` — Add what-if scenario method

**Harvesting Logic:**
1. For each holding, calculate unrealized gain/loss using average buy price vs current price
2. Filter for holdings with unrealized losses > ₹500
3. Calculate potential tax saving: `loss × taxRate` (20% for STCG, 12.5% for LTCG)
4. Consider: India has NO wash sale rule — can rebuy immediately
5. But flag GAAR risk for same-day buyback of same stock
6. Suggest: "Sell [STOCK] and consider buying [SIMILAR_STOCK or same stock after 1 trading day]"
7. Factor in ₹1.25L LTCG exemption — don't harvest if LTCG gains are still under exemption

**What-If Scenario:**
- Input: stock, quantity to sell
- Output: tax impact (LTCG or STCG), net proceeds, impact on LTCG exemption utilization

**Scheduler:** Run every Sunday at 10 AM IST. Expire old opportunities when prices change significantly.

**Acceptance Criteria:**
- Weekly scan identifies harvest opportunities
- `GET /api/tax/harvest-opportunities` returns active opportunities
- `GET /api/tax/what-if` calculates tax impact of hypothetical trades
- Notifications created for significant opportunities (saving > ₹1000)

---

#### Task 2.2: Daily AI Briefing

**Context:** Generate a personalized daily briefing using AI, considering portfolio, market data, and goals.

**Files to Create:**
- `src/main/java/com/portfolio/entity/DailyBriefing.java`
- `src/main/java/com/portfolio/repository/DailyBriefingRepository.java`
- `src/main/java/com/portfolio/service/DailyBriefingService.java`
- `src/main/java/com/portfolio/service/scheduler/DailyBriefingScheduler.java`
- `src/main/java/com/portfolio/dto/response/DailyBriefingResponse.java`

**Files to Modify:**
- `AIController.java` — Add briefing endpoints
- `AIAnalysisService.java` — Add briefing generation method (route to local model by default)

**Briefing Structure:**
```
Good morning, Markandey! Here's your portfolio briefing for April 5, 2026.

📊 Portfolio Summary
Your portfolio is valued at ₹12,45,000 (+₹8,500 / +0.69% yesterday).
XIRR: 18.4% | Nifty 50: 14.2% | Alpha: +4.2%

📈 Top Movers
↑ RELIANCE: ₹2,850 (+3.2%) — Oil prices up on OPEC cut
↓ INFY: ₹1,420 (-1.8%) — IT sector under pressure on USD weakness

⚠️ Alerts
• TCS approaching LTCG threshold (held 11 months, 3 days) — consider holding 27 more days for ₹8,000 tax saving
• IT sector allocation (28%) above your target (20%)

🎯 Goal Progress
• Retirement: ₹45L / ₹2Cr (22.5%) — on track with current SIP
• Emergency Fund: ₹3L / ₹5L (60%) — ₹2L more needed

💡 AI Insight
Consider reducing IT exposure by 8% and increasing Banking (currently 12%, underweight vs Nifty 50 Banking weight of 18%).
```

**Model Routing:** Use Ollama (Qwen 32B) for daily briefings. Fall back to Claude if Ollama is unavailable.

**Scheduler:** Run at user's preferred briefing time (default 6:00 AM IST).

**Acceptance Criteria:**
- Daily briefing generated automatically
- `GET /api/ai/briefing/today` returns today's briefing
- `GET /api/ai/briefing/history` returns past briefings
- Notification created with briefing summary

---

#### Task 2.3: Enhanced AI Chat with Memory and Context

**Context:** Upgrade the existing chat to include user preferences, goals, performance metrics, and tax situation in every AI context.

**Files to Modify:**
- `AIAnalysisService.java` — Enhance `buildPortfolioContext()` to include:
  - Risk profile and investment horizon from UserPreference
  - XIRR and alpha from latest PerformanceSnapshot
  - Financial goals summary
  - Current FY tax situation (LTCG/STCG gains so far)
  - Recent notifications/alerts
  - User's preferred sectors

- `ChatHistoryService.java` — Add method to extract "learned facts" from conversation history:
  - Parse past conversations for user-stated preferences
  - "I prefer large-cap stocks" → Store as preference
  - "I'm planning to buy a house in 2 years" → Suggest creating a goal

**System Prompt Enhancement:**
```
You are a personal finance advisor for an Indian retail investor.

## Investor Profile
- Risk Profile: {riskProfile} (Score: {riskScore}/10)
- Tax Slab: {taxSlab}%
- Investment Horizon: {horizon}
- Preferred Sectors: {sectors}

## Portfolio Performance
- Portfolio Value: ₹{value}
- XIRR: {xirr}% (vs Nifty 50: {niftyReturn}%)
- Alpha: {alpha}%

## Current Tax Situation (FY {fy})
- Realized LTCG: ₹{ltcg} (₹{ltcgExemptionRemaining} of ₹1.25L exemption remaining)
- Realized STCG: ₹{stcg}
- Tax payable so far: ₹{taxPayable}

## Financial Goals
{goalsSummary}

## Guidelines
- All amounts in ₹ (Indian Rupees)
- Consider Indian tax laws (LTCG 12.5% after ₹1.25L, STCG 20%)
- India has NO wash sale rule for tax-loss harvesting
- Reference XIRR (not simple returns) for performance
- Be specific with actionable suggestions
- Always include risk disclaimers for specific stock recommendations
- Format for mobile reading (short paragraphs, use bullets sparingly)
```

**Acceptance Criteria:**
- AI chat responses reference user's risk profile, goals, and tax situation
- Chat quality measurably improves with richer context
- No increase in latency (new context adds ~500 tokens)

---

### Task Group 3: Goals & Net Worth (Week 3-4)

---

#### Task 3.1: Financial Goal Planner

**Context:** Create goal management with projections and SIP calculations.

**Files to Create:**
- `src/main/java/com/portfolio/entity/FinancialGoal.java`
- `src/main/java/com/portfolio/entity/GoalAllocation.java`
- `src/main/java/com/portfolio/entity/GoalType.java` (enum)
- `src/main/java/com/portfolio/repository/FinancialGoalRepository.java`
- `src/main/java/com/portfolio/repository/GoalAllocationRepository.java`
- `src/main/java/com/portfolio/service/GoalService.java`
- `src/main/java/com/portfolio/controller/GoalController.java`
- `src/main/java/com/portfolio/dto/request/CreateGoalRequest.java`
- `src/main/java/com/portfolio/dto/request/CreateGoalAllocationRequest.java`
- `src/main/java/com/portfolio/dto/response/FinancialGoalResponse.java`
- `src/main/java/com/portfolio/dto/response/GoalProjectionResponse.java`

**Projection Calculations:**
- Monthly SIP needed: `SIP = FV × r / ((1+r)^n - 1)` where FV = target adjusted for inflation, r = monthly rate, n = months
- Three scenarios: pessimistic (return - 4%), expected, optimistic (return + 4%)
- On-track calculation: compare current_amount + projected SIP growth vs target_amount

**Goal Types with Defaults:**
- RETIREMENT: horizon 20-30 years, expected return 12%, inflation 6%
- HOUSE: horizon 3-7 years, expected return 10%, inflation 8% (real estate)
- EDUCATION: horizon 5-18 years, expected return 11%, inflation 10% (education inflation in India)
- EMERGENCY: horizon 6-12 months, expected return 6% (liquid fund), no inflation
- WEALTH: flexible, expected return 12%, inflation 6%

**Acceptance Criteria:**
- CRUD for goals with validation
- Goal projections with three scenarios
- Goal summary endpoint showing overall progress
- Link portfolios/assets to goals via allocations

---

#### Task 3.2: Net Worth Dashboard

**Context:** Track all assets (not just equities) for a complete financial picture.

**Files to Create:**
- `src/main/java/com/portfolio/entity/NetWorthAsset.java`
- `src/main/java/com/portfolio/repository/NetWorthAssetRepository.java`
- `src/main/java/com/portfolio/service/NetWorthService.java`
- `src/main/java/com/portfolio/controller/NetWorthController.java`
- `src/main/java/com/portfolio/dto/request/CreateNetWorthAssetRequest.java`
- `src/main/java/com/portfolio/dto/response/NetWorthResponse.java`
- `src/main/java/com/portfolio/dto/response/NetWorthAllocationResponse.java`

**Net Worth Calculation:**
- Auto-include: equity portfolio value (from PortfolioService), mutual fund holdings (if synced)
- Manual add: FD, PPF, EPF, NPS, gold, real estate, savings accounts, crypto
- Breakdown by: asset type, liquidity (liquid/illiquid), risk level

**Acceptance Criteria:**
- CRUD for manual assets
- `GET /api/networth` returns total with auto-included equities + manual assets
- Allocation breakdown by type, liquidity, and risk

---

#### Task 3.3: Mutual Fund Holdings via Kite

**Context:** Sync mutual fund holdings from Zerodha Coin via Kite API.

**Files to Create:**
- `src/main/java/com/portfolio/entity/MutualFundHolding.java`
- `src/main/java/com/portfolio/repository/MutualFundHoldingRepository.java`
- `src/main/java/com/portfolio/service/MutualFundService.java`
- `src/main/java/com/portfolio/dto/response/MutualFundHoldingResponse.java`

**Files to Modify:**
- `KiteClient.java` — Add `getMutualFundHoldings()` method using Kite MF API
- `KiteAuthController.java` — Add `/api/kite/mf-holdings` and `/api/kite/mf-sync` endpoints
- `HoldingsSyncService.java` — Add MF sync to the auto-sync schedule

**Acceptance Criteria:**
- MF holdings synced from Kite
- Displayed in net worth calculations
- Included in AI context

---

### Task Group 4: Frontend Implementation (Week 4-6)

---

#### Task 4.1: Performance Page

**Context:** Build the Performance page with XIRR chart, benchmark comparison, and daily P&L.

**Files to Create (Frontend):**
- `src/pages/Performance.tsx`
- `src/components/performance/PerformanceChart.tsx`
- `src/components/performance/XIRRCard.tsx`
- `src/components/performance/DailyPnLCard.tsx`
- `src/components/performance/SectorPerformance.tsx`
- `src/components/performance/BenchmarkSelector.tsx`
- `src/api/performance.ts`

**Files to Modify:**
- `src/App.tsx` — Add route `/performance`
- `src/components/layout/Sidebar.tsx` — Add Performance nav item

**UI Design:**
- Top row: 4 metric cards (XIRR, CAGR, Alpha, Day Change)
- Main chart: Line chart with portfolio value vs Nifty 50, period selector (1M, 3M, 6M, 1Y, 3Y, ALL)
- Below chart: Sector performance breakdown table
- Use existing dark theme, Recharts for charts

**Acceptance Criteria:**
- Performance page renders with XIRR, benchmark comparison chart
- Period selector works
- Responsive for mobile

---

#### Task 4.2: Goals Page

**Context:** Build the Goals page with goal cards, progress tracking, and SIP calculator.

**Files to Create (Frontend):**
- `src/pages/Goals.tsx`
- `src/components/goals/GoalCard.tsx`
- `src/components/goals/GoalForm.tsx`
- `src/components/goals/GoalProjection.tsx`
- `src/components/goals/SIPCalculator.tsx`
- `src/api/goals.ts`
- `src/store/goalStore.ts`

**Files to Modify:**
- `src/App.tsx` — Add route `/goals`
- `src/components/layout/Sidebar.tsx` — Add Goals nav item

**Acceptance Criteria:**
- Goals CRUD with form modal
- Progress bars on goal cards
- Projection chart with 3 scenarios
- Inline SIP calculator

---

#### Task 4.3: Net Worth Page

**Files to Create (Frontend):**
- `src/pages/NetWorth.tsx`
- `src/components/networth/NetWorthSummary.tsx`
- `src/components/networth/AssetAllocationChart.tsx`
- `src/components/networth/AssetForm.tsx`
- `src/components/networth/LiquidityBreakdown.tsx`
- `src/api/networth.ts`

**Acceptance Criteria:**
- Net worth total with auto-included equity + manual assets
- Donut chart by asset type
- CRUD for manual assets

---

#### Task 4.4: Notification System (Frontend)

**Files to Create (Frontend):**
- `src/components/common/NotificationBell.tsx`
- `src/components/common/NotificationPanel.tsx`
- `src/api/notifications.ts`
- `src/store/notificationStore.ts`

**Files to Modify:**
- `src/components/layout/AppLayout.tsx` — Add NotificationBell to header
- `src/hooks/useWebSocket.ts` — Subscribe to `/topic/notifications/{userId}`

**Acceptance Criteria:**
- Bell icon with unread count badge in header
- Click opens slide-over panel with notification list
- Real-time push via WebSocket
- Mark as read functionality

---

#### Task 4.5: Daily Briefing Card on Dashboard

**Files to Create (Frontend):**
- `src/components/dashboard/DailyBriefingCard.tsx`
- `src/components/dashboard/TaxHarvestAlert.tsx`

**Files to Modify:**
- `src/pages/Dashboard.tsx` — Add DailyBriefingCard and TaxHarvestAlert above existing content

**Acceptance Criteria:**
- Morning briefing card on dashboard (expandable)
- Tax harvest alert banner when opportunities exist
- Briefing links to relevant pages (performance, tax, goals)

---

#### Task 4.6: Risk Assessment Questionnaire & Settings Page

**Files to Create (Frontend):**
- `src/pages/Settings.tsx`
- `src/components/settings/RiskQuestionnaire.tsx`
- `src/components/settings/PreferenceForm.tsx`
- `src/components/settings/NotificationSettings.tsx`
- `src/api/profile.ts`
- `src/store/preferenceStore.ts`

**Files to Modify:**
- `src/App.tsx` — Add route `/settings`
- `src/components/layout/Sidebar.tsx` — Add Settings nav item at bottom

**Acceptance Criteria:**
- Multi-step risk questionnaire (10 questions)
- Score display with risk profile explanation
- Preference editing (tax slab, income range, briefing time, notification toggles)

---

#### Task 4.7: Watchlist Page

**Files to Create (Frontend):**
- `src/pages/Watchlist.tsx`
- `src/components/watchlist/WatchlistCard.tsx`
- `src/components/watchlist/AddToWatchlistModal.tsx`
- `src/api/watchlists.ts`

**Files to Modify:**
- `src/App.tsx` — Add route `/watchlist`
- `src/components/layout/Sidebar.tsx` — Add Watchlist nav item
- `src/components/portfolio/HoldingsTable.tsx` — Add "Add to Watchlist" action

**Acceptance Criteria:**
- Watchlist with live prices
- Set target buy/sell prices
- "Add to Watchlist" from holdings table

---

### Task Group 5: Production Readiness (Week 6-8)

---

#### Task 5.1: Rate Limiting & Usage Tracking

**Context:** Prevent AI cost overrun and prepare for multi-user.

**Implementation:**
- Add `@RateLimiter` annotation using Bucket4j or custom interceptor
- Rate limits: 10 AI analyses/day, 100 chat messages/day, 5 rebalancing/day
- Track AI usage per user per day in a `ai_usage_log` table
- Return 429 with retry-after header when exceeded

---

#### Task 5.2: Onboarding Flow

**Context:** First-time user experience.

**Flow:**
1. Register/Login
2. Connect Zerodha Kite (optional, can skip)
3. Risk questionnaire (10 questions)
4. Set financial goals (optional, can skip)
5. Dashboard

---

#### Task 5.3: Multi-tenancy Hardening

**Context:** Ensure complete data isolation between users.

**Implementation:**
- Audit every repository method for user_id filtering
- Add Spring Security method-level authorization
- Verify no cross-user data leakage in AI context
- Add `@PreAuthorize` on sensitive endpoints

---

## 12. Market Positioning

### Unique Differentiators vs. Indian Competitors

| Feature | PortfolioAI (Us) | INDmoney | Groww | Smallcase | Kuvera |
|---------|-------------------|----------|-------|-----------|--------|
| True AI Advisory (LLM) | ✅ Claude + Local | ❌ Rule-based | ❌ None | ❌ None | ❌ Rule-based |
| Kite Integration | ✅ Native OAuth | ❌ Own broker | ❌ Own broker | ✅ Multiple | ❌ Multiple |
| Tax-Loss Harvesting | ✅ AI-powered | ❌ Basic | ❌ None | ❌ None | ⚠️ Manual |
| XIRR Performance | ✅ With benchmark | ✅ Basic | ✅ Basic | ❌ None | ✅ Good |
| AI Chat with Web Search | ✅ SSE + RAG | ❌ None | ❌ None | ❌ None | ❌ None |
| Goal Planning | ✅ AI-driven projections | ✅ Rule-based | ⚠️ Basic | ❌ None | ✅ Good |
| Analysis Transparency | ✅ Full (prompts visible) | ❌ Black box | ❌ None | ❌ None | ❌ None |
| Daily AI Briefing | ✅ Personalized | ❌ Generic | ❌ Generic | ❌ None | ❌ None |
| Open Source / Self-Host | ✅ Possible | ❌ No | ❌ No | ❌ No | ❌ No |
| SEBI RIA | ⏳ Phase 2 | ✅ Registered | ⚠️ Settlement | ✅ Platform | ✅ Registered |

### Positioning Statement

**"PortfolioAI is the first AI-native personal finance advisor for Indian investors — built with real AI that explains its reasoning, integrated with Zerodha, and designed to save you real money on taxes."**

### Target User Profile

- Indian retail investor, 25-45 years old
- Uses Zerodha for trading
- Has 5-50 stocks across 1-3 portfolios
- Portfolio value ₹5L-₹2Cr
- Wants to optimize returns and minimize taxes
- Comfortable with technology but not a quant

### Pricing Strategy (Phase 3)

| Tier | Price | Features |
|------|-------|----------|
| Free | ₹0 | Portfolio tracking, basic analytics, 5 AI chats/day |
| Pro | ₹299/month | Full AI advisory, daily briefings, tax optimization, goals, unlimited chat |
| Premium | ₹599/month | Everything + priority AI (Claude), portfolio simulation, export reports |

---

## 13. Cost Model

### Infrastructure Cost (Personal Use)

| Component | Monthly Cost |
|---|---|
| Mac Mini M4 Pro (Ollama, PostgreSQL) | ₹0 (owned hardware) |
| Anthropic API (~38 cloud calls/month) | ~₹140 ($1.70) |
| MySQL (local Docker) | ₹0 |
| Domain + SSL | ~₹100 |
| **Total** | **~₹240/month** |

### Infrastructure Cost (Production — 100 users)

| Component | Monthly Cost |
|---|---|
| AWS EC2 t3.small | ₹1,500 ($18) |
| RDS MySQL db.t3.micro | ₹1,250 ($15) |
| RDS PostgreSQL db.t3.micro | ₹1,250 ($15) |
| Anthropic API (3,800 cloud calls) | ₹14,000 ($170) |
| S3 + CloudFront | ₹250 ($3) |
| Domain + SSL | ₹100 |
| **Total** | **~₹18,350/month** |

**Revenue at 100 users (50 Pro, 10 Premium):**
- 50 × ₹299 + 10 × ₹599 = ₹14,950 + ₹5,990 = **₹20,940/month**
- **Profitable at ~100 users** with current pricing

---

## 14. Risk Register

| Risk | Impact | Probability | Mitigation |
|---|---|---|---|
| SEBI regulatory action | High | Medium | Launch as information platform (Track A), disclaim all AI outputs |
| Kite API rate limiting | Medium | Low | Cache aggressively, batch requests, respect rate limits |
| AI hallucination in financial advice | High | Medium | Transparency panel, disclaimers, human-in-the-loop for trades |
| Anthropic API cost overrun | Medium | Low | 88% local model routing, rate limiting, usage tracking |
| Data breach (financial data) | Critical | Low | Encrypt PAN, JWT rotation, HTTPS everywhere, audit logs |
| Zerodha changes API terms | Medium | Low | Abstract Kite integration behind interface, support manual CSV as fallback |
| Local model quality insufficient | Medium | Medium | Fallback to Claude, A/B test response quality, upgrade to larger models as hardware allows |
| User adoption too slow | Medium | High | Focus on personal use value first, share with Zerodha trading communities |

---

## Appendix A: File Structure After Implementation

```
applications/portfolio-tracker/
├── src/main/java/com/portfolio/
│   ├── config/
│   │   ├── (existing configs...)
│   │   └── RateLimitConfig.java                   [NEW]
│   ├── controller/
│   │   ├── (existing controllers...)
│   │   ├── PerformanceController.java             [NEW]
│   │   ├── GoalController.java                    [NEW]
│   │   ├── NetWorthController.java                [NEW]
│   │   ├── WatchlistController.java               [NEW]
│   │   ├── NotificationController.java            [NEW]
│   │   └── ProfileController.java                 [NEW]
│   ├── entity/
│   │   ├── (existing entities...)
│   │   ├── UserPreference.java                    [NEW]
│   │   ├── FinancialGoal.java                     [NEW]
│   │   ├── GoalAllocation.java                    [NEW]
│   │   ├── GoalType.java                          [NEW]
│   │   ├── NetWorthAsset.java                     [NEW]
│   │   ├── MutualFundHolding.java                 [NEW]
│   │   ├── PerformanceSnapshot.java               [NEW]
│   │   ├── Watchlist.java                         [NEW]
│   │   ├── WatchlistItem.java                     [NEW]
│   │   ├── Notification.java                      [NEW]
│   │   ├── TaxHarvestOpportunity.java             [NEW]
│   │   └── DailyBriefing.java                     [NEW]
│   ├── repository/
│   │   ├── (existing repos...)
│   │   └── (12 new repository interfaces)         [NEW]
│   ├── service/
│   │   ├── (existing services...)
│   │   ├── PerformanceService.java                [NEW]
│   │   ├── GoalService.java                       [NEW]
│   │   ├── NetWorthService.java                   [NEW]
│   │   ├── WatchlistService.java                  [NEW]
│   │   ├── NotificationService.java               [NEW]
│   │   ├── UserPreferenceService.java             [NEW]
│   │   ├── MutualFundService.java                 [NEW]
│   │   ├── TaxHarvestService.java                 [NEW]
│   │   ├── DailyBriefingService.java              [NEW]
│   │   └── scheduler/
│   │       ├── PerformanceScheduler.java          [NEW]
│   │       ├── TaxHarvestScheduler.java           [NEW]
│   │       └── DailyBriefingScheduler.java        [NEW]
│   └── dto/
│       ├── request/
│       │   ├── (existing DTOs...)
│       │   └── (8 new request DTOs)               [NEW]
│       └── response/
│           ├── (existing DTOs...)
│           └── (12 new response DTOs)             [NEW]
│
applications/portfolio-tracker-frontend/
├── src/
│   ├── pages/
│   │   ├── (existing pages...)
│   │   ├── Performance.tsx                        [NEW]
│   │   ├── Goals.tsx                              [NEW]
│   │   ├── NetWorth.tsx                           [NEW]
│   │   ├── Watchlist.tsx                          [NEW]
│   │   └── Settings.tsx                           [NEW]
│   ├── components/
│   │   ├── (existing components...)
│   │   ├── performance/                           [NEW - 5 components]
│   │   ├── goals/                                 [NEW - 4 components]
│   │   ├── networth/                              [NEW - 4 components]
│   │   ├── watchlist/                             [NEW - 2 components]
│   │   ├── settings/                              [NEW - 3 components]
│   │   ├── dashboard/DailyBriefingCard.tsx        [NEW]
│   │   ├── dashboard/TaxHarvestAlert.tsx          [NEW]
│   │   └── common/NotificationBell.tsx            [NEW]
│   │   └── common/NotificationPanel.tsx           [NEW]
│   ├── api/
│   │   ├── (existing API files...)
│   │   ├── performance.ts                         [NEW]
│   │   ├── goals.ts                               [NEW]
│   │   ├── networth.ts                            [NEW]
│   │   ├── watchlists.ts                          [NEW]
│   │   ├── notifications.ts                       [NEW]
│   │   └── profile.ts                             [NEW]
│   └── store/
│       ├── (existing stores...)
│       ├── goalStore.ts                           [NEW]
│       ├── notificationStore.ts                   [NEW]
│       └── preferenceStore.ts                     [NEW]
```

---

## Appendix B: Implementation Priority Summary

**If you only have 2 weeks, build these (highest ROI for personal use):**

1. **XIRR & Performance** (Task 1.2) — Know your actual returns
2. **Tax-Loss Harvesting** (Task 2.1) — Save real money
3. **Daily Briefing** (Task 2.2) — Daily value, replaces manual checking
4. **Enhanced AI Context** (Task 2.3) — Makes every AI interaction smarter
5. **Performance Page** (Task 4.1) — Visualize your returns

**These 5 tasks alone transform the portfolio tracker into a personal finance advisor.**

---

*End of Design Document*
