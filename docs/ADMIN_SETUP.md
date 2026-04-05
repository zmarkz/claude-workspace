# Portfolio Intelligence Platform — Admin Setup & Cost Guide

> **Last updated**: 2026-04-06 | **For**: App administrator (Markandey Singh)

---

## Current Status

| Component | Status | Notes |
|-----------|--------|-------|
| Backend (Spring Boot) | Running | Docker, port 8080 |
| Frontend (React) | Running | Docker, port 5173 |
| MySQL | Running | Local, port 3306, database: `portfolio` |
| PostgreSQL | Running | Local, port 5432, database: `mcp_farm` |
| Redis | Running | Docker, port 6379 |
| MCP Gateway | Running | Docker, port 9080/8081 |
| Agent Farm | Running | Docker, port 8082 |
| Claude API Key | Set | `ANTHROPIC_API_KEY` configured |
| Broker APIs | Not configured | No broker API keys set yet |

---

## Manual Actions Required

### Priority 1: Already Done (App Works)

These are already configured — no action needed:

- [x] MySQL database `portfolio` created and running
- [x] PostgreSQL database `mcp_farm` created and running
- [x] Redis running in Docker
- [x] JWT secret configured
- [x] `ANTHROPIC_API_KEY` set for Claude AI
- [x] `AGENT_FARM_API_KEY` set (default: `admin-secret`)
- [x] All Docker services running
- [x] Test user created (testuser@portfolio.ai / Test@123)
- [x] Admin user created (admin@portfolio.com)

### Priority 2: Broker API Keys (Optional — enables auto-sync)

Set these in `~/Documents/claude/platform/.env` then restart Docker.

#### Free Brokers (register and get keys immediately)

| Broker | Registration URL | Env Vars to Set | Cost |
|--------|-----------------|-----------------|------|
| **Upstox** | https://developer.upstox.com/ | `UPSTOX_API_KEY`, `UPSTOX_API_SECRET` | Free |
| **Angel One** | https://smartapi.angelone.in/ | `ANGEL_API_KEY`, `ANGEL_API_SECRET` | Free |
| **5paisa** | https://openapi.5paisa.com/ | `FIVEPAISA_API_KEY`, `FIVEPAISA_API_SECRET` | Free |
| **ICICI Direct** | https://api.icicidirect.com/ | `ICICI_API_KEY`, `ICICI_API_SECRET` | Free |

#### Paid Broker

| Broker | Registration URL | Env Vars to Set | Cost |
|--------|-----------------|-----------------|------|
| **Zerodha** | https://developers.kite.trade/ | `KITE_API_KEY`, `KITE_API_SECRET` | ₹2,000/month |

#### No API (CSV upload only — no keys needed)

- **Groww** — users upload portfolio CSV from Groww app
- **Paytm Money** — users upload portfolio CSV from Paytm app

#### Steps to Configure a Broker

```bash
# 1. Register at the broker's developer portal (URLs above)
# 2. Create an app with these settings:
#    - App Name: PortfolioAI
#    - Redirect URL: http://localhost:8080/api/brokers/{BROKER_TYPE}/callback
#      (e.g., http://localhost:8080/api/brokers/ZERODHA/callback)
#    - For production: https://your-domain.com/api/brokers/{BROKER_TYPE}/callback
# 3. Copy API Key and Secret
# 4. Add to platform/.env:

cd ~/Documents/claude/platform
cat >> .env << 'EOF'
KITE_API_KEY=your_zerodha_key
KITE_API_SECRET=your_zerodha_secret
UPSTOX_API_KEY=your_upstox_key
UPSTOX_API_SECRET=your_upstox_secret
ANGEL_API_KEY=your_angel_key
ANGEL_API_SECRET=your_angel_secret
EOF

# 5. Restart backend:
docker compose up -d portfolio-tracker
```

### Priority 3: Production Deployment (when ready to go live)

| Action | How | Cost |
|--------|-----|------|
| Domain name | Buy from GoDaddy/Namecheap/Cloudflare | ~₹800/year |
| SSL certificate | Let's Encrypt (free) via certbot | Free |
| AWS EC2 | Launch t3.small, install Docker | ~₹1,500/mo |
| AWS RDS MySQL | db.t3.micro, free tier eligible | ~₹1,250/mo |
| AWS RDS PostgreSQL | db.t3.micro, free tier eligible | ~₹1,250/mo |
| AWS ElastiCache | t3.micro Redis | ~₹800/mo |
| Production env file | Create `platform/.env.prod` with real secrets | — |
| HTTPS in Nginx | Enable SSL block in nginx.conf | — |

---

## Monthly Cost Breakdown

### Scenario A: Personal Use (current)

| Item | Cost |
|------|------|
| All infrastructure | ₹0 (runs on your Mac) |
| Claude API (~38 calls/mo) | ~₹140 |
| **TOTAL** | **~₹140/mo** |

### Scenario B: Personal + Zerodha Auto-Sync

| Item | Cost |
|------|------|
| All infrastructure | ₹0 |
| Claude API | ~₹140 |
| Zerodha Kite API | ₹2,000 |
| **TOTAL** | **~₹2,140/mo** |

### Scenario C: Personal + Free Brokers Only

| Item | Cost |
|------|------|
| All infrastructure | ₹0 |
| Claude API | ~₹140 |
| Upstox + Angel One + 5paisa + ICICI | ₹0 (all free) |
| **TOTAL** | **~₹140/mo** |

### Scenario D: Production (100 users)

| Item | Cost |
|------|------|
| AWS EC2 (t3.small) | ₹1,500 |
| RDS MySQL | ₹1,250 |
| RDS PostgreSQL | ₹1,250 |
| ElastiCache Redis | ₹800 |
| Claude API (~3,800 calls) | ₹14,000 |
| Zerodha Kite | ₹2,000 |
| Domain + SSL | ₹100 |
| S3 + CloudFront | ₹250 |
| **TOTAL** | **~₹21,150/mo** |
| **Revenue** (50 Pro + 10 Premium) | **₹20,940/mo** |
| **Break-even** | **~100 users** |

---

## Environment Variables — Complete Reference

### Required (app won't work without these)

```bash
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=portfolio
DB_USER=root
DB_PASSWORD=mark1234

# Auth
JWT_SECRET=portfolioTrackerSecretKey2024!@#ChangeInProd$%^&*(1234567890abcdefghijk

# AI (for analysis, chat, briefings)
ANTHROPIC_API_KEY=sk-ant-...

# Agent Farm
AGENT_FARM_URL=http://localhost:8082   # or http://agent-farm:8082 in Docker
AGENT_FARM_API_KEY=admin-secret

# Frontend CORS
FRONTEND_URL=http://localhost:5173
```

### Optional — Broker API Keys

```bash
# Zerodha (₹2,000/month)
KITE_API_KEY=
KITE_API_SECRET=

# Upstox (FREE)
UPSTOX_API_KEY=
UPSTOX_API_SECRET=

# Angel One (FREE)
ANGEL_API_KEY=
ANGEL_API_SECRET=

# 5paisa (FREE)
FIVEPAISA_API_KEY=
FIVEPAISA_API_SECRET=

# ICICI Direct (FREE)
ICICI_API_KEY=
ICICI_API_SECRET=
```

### Optional — Other Services

```bash
# OpenAI (alternative to Claude, not needed)
OPENAI_API_KEY=

# Admin Nexus (MCP Farm dashboard)
ADMIN_API_KEY=admin-secret

# MCP Farm
DATABASE_URL=postgres://markandeysingh@localhost:5432/mcp_farm
REDIS_URL=redis://localhost:6379
```

---

## Common Admin Tasks

### Restart after config change

```bash
cd ~/Documents/claude/platform
docker compose up -d portfolio-tracker
```

### Rebuild after code change

```bash
cd ~/Documents/claude/platform
docker compose build portfolio-tracker portfolio-tracker-frontend
docker compose up -d portfolio-tracker portfolio-tracker-frontend
```

### Check service health

```bash
docker ps --format "table {{.Names}}\t{{.Status}}\t{{.Ports}}"
curl -s http://localhost:8080/v3/api-docs | head -1  # backend
curl -s http://localhost:5173 | head -1               # frontend
```

### View logs

```bash
docker logs -f portfolio_tracker_api --tail 50
docker logs -f portfolio_tracker_frontend --tail 50
```

### Database access

```bash
/usr/local/mysql/bin/mysql -u root -pmark1234 portfolio
```

### Create a new admin user

```bash
curl -s http://localhost:8080/api/auth/register \
  -H 'Content-Type: application/json' \
  -d '{"email":"admin@example.com","password":"SecurePass123","firstName":"Admin","lastName":"User"}'
```

### Check what brokers are configured

```bash
TOKEN=$(curl -s http://localhost:8080/api/auth/login \
  -H 'Content-Type: application/json' \
  -d '{"email":"testuser@portfolio.ai","password":"Test@123"}' \
  | python3 -c "import sys,json; print(json.load(sys.stdin)['token'])")

curl -s http://localhost:8080/api/brokers/available \
  -H "Authorization: Bearer $TOKEN" | python3 -m json.tool
```

---

## Recommendation: Start Here

1. **Right now**: App works fully with manual CSV/CAS uploads. No additional cost.
2. **Next step**: Register for free broker APIs (Upstox, Angel One) — takes 10 minutes each.
3. **When you have users**: Add Zerodha (₹2K/mo) — covers the most traders.
4. **For production**: Deploy to AWS, buy domain, enable HTTPS.

---

## Files That Reference This Config

| File | What It Reads |
|------|--------------|
| `platform/docker-compose.yml` | All env vars for all services |
| `platform/.env` | Actual values (gitignored) |
| `platform/.env.prod.example` | Template for production |
| `portfolio-tracker/application.yml` | `kite.*`, `brokers.*`, `anthropic.*`, `agent-farm.*` |
| `portfolio-tracker/BrokersConfig.java` | Reads `brokers.*` properties |
| `portfolio-tracker/KiteConfig.java` | Reads `kite.*` (legacy) |
| `portfolio-tracker/AnthropicConfig.java` | Reads `anthropic.*` |
| `portfolio-tracker/AgentFarmConfig.java` | Reads `agent-farm.*` |
