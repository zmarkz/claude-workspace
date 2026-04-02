# PortfolioAI — Full-Stack Portfolio Tracker

A full-stack portfolio management application that integrates with **Zerodha (Kite Connect)** for holdings sync and **Claude AI (Anthropic)** for intelligent portfolio analysis, recommendations, and conversational insights.

## Architecture Overview

```
                                    +------------------+
                                    |   Zerodha Kite   |
                                    |   Connect API    |
                                    +--------+---------+
                                             |
+-------------------+    REST/SSE    +-------+--------+    JPA/Hibernate    +----------+
|  React Frontend   | <-----------> | Spring Boot API | <----------------> |  MySQL   |
|  (Vite + TS)      |    :5173      |    :8080        |                    | Database |
+-------------------+               +-------+--------+                    +----------+
        |                                    |
        | WebSocket (STOMP)                  | HTTP (Direct)
        +------------------------------------+
                                             |
                                    +--------+---------+
                                    |  Anthropic API   |
                                    |  (Claude Sonnet) |
                                    +------------------+
```

**Frontend**: React 19 + TypeScript + Vite 8 + TailwindCSS 4
**Backend**: Spring Boot 3.2 + Java 23 + Hibernate + Spring Security
**Database**: MySQL 8+
**AI**: Claude Sonnet 4.6 via Anthropic REST API
**Broker**: Zerodha Kite Connect (Personal plan — free)

---

## Database Schema

```
users
  id (PK), email (UNIQUE), password_hash, first_name, last_name,
  role (ENUM: USER, ADMIN), kite_access_token,
  is_active, created_at, updated_at, last_login_at

portfolios
  id (PK), user_id (FK -> users), name, description, currency,
  created_at, updated_at

stocks
  id (PK), symbol (UNIQUE), company_name, exchange, sector, industry,
  current_price, last_updated_at

portfolio_holdings
  id (PK), portfolio_id (FK -> portfolios), stock_id (FK -> stocks),
  quantity, average_buy_price, created_at, updated_at

transactions
  id (PK), portfolio_id (FK -> portfolios), stock_id (FK -> stocks),
  transaction_type (ENUM: BUY, SELL), quantity, price, total_amount,
  transaction_date, notes

stock_price_history
  id (PK), stock_id (FK -> stocks), price_date,
  open_price, high_price, low_price, close_price, volume

alerts
  id (PK), user_id (FK -> users), alert_type,
  stock_id (FK -> stocks), portfolio_id (FK -> portfolios),
  threshold_value, comparison_operator, is_triggered,
  last_triggered_at, created_at, updated_at
```

### Key Relationships
- User 1:N Portfolio
- Portfolio 1:N PortfolioHolding, Portfolio 1:N Transaction
- Stock 1:N PortfolioHolding, Stock 1:N Transaction, Stock 1:N StockPriceHistory

### Hibernate Notes
- All entities use `@Getter @Setter @EqualsAndHashCode(onlyExplicitlyIncluded = true)` — **not** `@Data` — to avoid circular reference issues in bidirectional JPA relationships. Only the `@Id` field is included in equals/hashCode.
- `Portfolio.holdings` and `Portfolio.transactions` use `@Builder.Default` to initialize collections, since Lombok's `@Builder` ignores field initializers.
- `PortfolioRepository.findByIdWithHoldings()` uses `LEFT JOIN FETCH` to eagerly load holdings + stocks in a single query, avoiding N+1 problems.

---

## API Endpoints

### Authentication (public)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register user, returns JWT (24h expiry) |
| POST | `/api/auth/login` | Authenticate, returns JWT |

### Portfolio Management (authenticated)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/portfolios` | Create portfolio (requires userId) |
| GET | `/api/portfolios/{id}` | Get portfolio with holdings |
| GET | `/api/portfolios/{id}/analysis` | Computed metrics: total value, P&L, sector allocation (cached) |
| POST | `/api/portfolios/{id}/holdings` | Add holding (merges with weighted avg if duplicate) |
| POST | `/api/portfolios/{id}/holdings/upload` | Upload CSV or XLSX file (Zerodha Console format supported) |
| DELETE | `/api/portfolios/{pid}/holdings/{hid}` | Remove a holding |

### Stock Management (authenticated)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/stocks/search?query=&limit=` | Search by symbol or company name |
| GET | `/api/stocks/{symbol}/price` | Current cached price |
| GET | `/api/stocks/{symbol}/analysis` | Technical analysis: SMA50, SMA200, RSI, MACD |

### Transactions (authenticated)
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/transactions` | Execute BUY/SELL, updates holding avg price |
| GET | `/api/portfolios/{id}/transactions` | Transaction history (newest first) |

### AI Analysis (authenticated, requires ANTHROPIC_API_KEY)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/ai/status` | Check if Claude API is configured |
| POST | `/api/ai/portfolio/{id}/analyze` | Full AI analysis: health score 1-10, risks, opportunities, recommendations |
| GET | `/api/ai/portfolio/{id}/rebalance` | Rebalancing suggestions with target weights |
| GET | `/api/ai/portfolio/{pid}/stock/{symbol}` | Single stock BUY/HOLD/SELL recommendation |
| POST | `/api/ai/portfolio/{id}/chat` | SSE streaming chat with portfolio context |

### Kite Connect / Zerodha (authenticated, requires KITE_API_KEY)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/kite/login` | Redirects to Zerodha OAuth page |
| GET | `/api/kite/callback` | OAuth callback, exchanges request_token for access_token |
| GET | `/api/kite/status` | Check connection status + configured state |
| POST | `/api/kite/connect` | Manually store access token |
| POST | `/api/kite/sync?portfolioId=` | Fetch and upsert holdings from Zerodha |

### WebSocket
| Endpoint | Description |
|----------|-------------|
| `/ws` | STOMP over SockJS |
| `/topic/prices` | Subscribe for live price broadcasts (`Map<String, BigDecimal>`) |

---

## AI Integration (Claude API)

### How It Works
- Direct HTTP calls to `https://api.anthropic.com/v1/messages` using Java's `HttpClient`
- No Anthropic SDK — raw REST with `x-api-key` and `anthropic-version: 2023-06-01` headers
- Model: `claude-sonnet-4-6`, Max tokens: 2048

### Portfolio Context Sent to Claude
Every AI request includes the full portfolio as context:
```
Portfolio: My Portfolio
Total Value: Rs.6,57,614 | Total P&L: Rs.68,374 (11.6%)
Holdings:
  - RELIANCE (Reliance Industries): qty=25, avgBuy=Rs.2650, current=Rs.2945, P&L=Rs.7387 (11.2%), sector=Energy
  - TCS (Tata Consultancy): qty=15, avgBuy=Rs.3450, current=Rs.3820, P&L=Rs.5561 (10.7%), sector=Technology
  ...
```

### AI Endpoints and Prompts

**Portfolio Analysis** (`POST /api/ai/portfolio/{id}/analyze`)
- System prompt: "You are an expert financial advisor specializing in Indian equity markets"
- Asks for JSON with: healthScore (1-10), summary, riskProfile (Low/Medium/High), risks[], opportunities[], stockRecommendations[] with action (BUY/HOLD/SELL), rationale, confidence
- Non-streaming response, parsed as JSON

**Rebalancing** (`GET /api/ai/portfolio/{id}/rebalance`)
- System prompt: "You are an expert portfolio manager specializing in Indian equities"
- Returns JSON array: symbol, currentWeightPercent, targetWeightPercent, action (INCREASE/DECREASE/HOLD), reason
- Non-streaming response

**Stock Recommendation** (`GET /api/ai/portfolio/{pid}/stock/{symbol}`)
- Analyzes a single stock in the context of the overall portfolio
- Returns: symbol, action, rationale, confidence

**Chat** (`POST /api/ai/portfolio/{id}/chat`)
- SSE streaming via `SseEmitter`
- System message includes portfolio context
- Supports conversation history (last 10 messages)
- Events: `event: token` (content chunk), `event: done` (stream end), `event: error` (failure)

### Graceful Degradation
- If `ANTHROPIC_API_KEY` is not set, all AI endpoints return 503 with `AI_NOT_CONFIGURED` error code
- Frontend shows descriptive error messages instead of crashing
- Chat SSE sends `event: error` with message before completing the emitter

---

## Zerodha / Kite Connect Integration

### OAuth Flow
```
1. User clicks "Connect Zerodha" on frontend
2. Frontend opens: GET /api/kite/login
3. Backend redirects to: https://kite.zerodha.com/connect/login?api_key=XXX
4. User logs into Zerodha, authorizes the app
5. Zerodha redirects to: GET /api/kite/callback?request_token=YYY
6. Backend computes checksum: SHA-256(api_key + request_token + api_secret)
7. Backend POSTs to https://api.kite.trade/session/token to exchange for access_token
8. Access token stored in user.kiteAccessToken in DB
9. Backend redirects to frontend: /dashboard?kite_connected=true&token=...
```

### Holdings Sync
- **Manual**: User clicks "Sync Holdings" → `POST /api/kite/sync?portfolioId=1`
- **Automatic**: `HoldingsSyncService` runs every 15 minutes for all users with a stored Kite access token
- Fetches `GET /portfolio/holdings` from Kite API
- Upserts holdings: creates stock if not exists, updates quantity and avg price if exists
- Evicts portfolio analysis cache after sync

### Free (Personal) Plan Limitations
- Holdings, positions, and reports APIs: **available**
- Historical chart data: **not available**
- Live market quotes and WebSocket ticks: **not available**
- The 5-second live price poller (`StockService.updateLivePrices`) is **disabled** since it requires the paid plan
- Prices are updated from holdings data (previous closing price) during sync

### Kite Connect Setup
1. Go to https://developers.kite.trade/ → Create app → Select "Personal" (Free)
2. Redirect URL: `http://127.0.0.1:5173/api/kite/callback`
3. Start backend with: `KITE_API_KEY=xxx KITE_API_SECRET=yyy mvn spring-boot:run`
4. Do the OAuth login from a Mac browser at `http://localhost:5173`
5. After connecting, holdings sync works from any device (phone, etc.)

---

## CSV / Excel Upload

The app supports uploading holdings from Zerodha Console's XLSX export or any CSV file.

### Supported Formats
- **Zerodha Console XLSX**: Auto-detects header row (skips banner/summary rows), maps columns by name
- **Generic CSV**: `Symbol, Quantity, Average Price [, LTP]` (minimum 3 columns)
- Auto-detects comma vs tab delimiters

### Zerodha Console XLSX Format
```
Row 0: [banner text]
Row 1: Client ID, YH0840
Row 2: Equity Holdings Statement as on 2026-04-02
Row 3: Summary
...
Row 8: [HEADER] Symbol, ISIN, Sector, Quantity Available, ..., Average Price, Previous Closing Price, ...
Row 9+: [DATA] RELIANCE, INE002A01018, Energy, 25, ..., 2650.00, 2945.50, ...
```

### Column Detection Priority
- **Symbol**: "instrument", "symbol", "stock", "scrip"
- **Quantity**: "quantity available", "qty" (excludes "pledged", "discrepant", "long term")
- **Average Price**: "average price", "avg", "buy price", "cost"
- **LTP**: "previous closing", "ltp", "last traded", "close price"

### Upload Behavior
- `replace=true` (default): Deletes existing holdings before importing
- `replace=false`: Merges with existing holdings using weighted average price
- Stocks not in the database are auto-created with exchange="NSE"
- Evicts portfolio analysis cache after upload

---

## Frontend Architecture

### Tech Stack
- **React 19** with functional components and hooks
- **TypeScript 5.9** with strict mode
- **Vite 8** for dev server and bundling
- **TailwindCSS 4** via `@tailwindcss/vite` plugin
- **Zustand 5** for state management (auth + portfolio stores)
- **TanStack React Query 5** for server state (caching, refetching)
- **Axios** for HTTP with JWT interceptor
- **Recharts** for charts (sector allocation pie chart)
- **Lucide React** for icons
- **STOMP.js + SockJS** for WebSocket price updates

### Routing (React Router v7)
```
/login          → Login/Register page (public)
/               → Redirects to /dashboard
/dashboard      → Portfolio overview, metrics, holdings table, sector chart, CSV upload
/holdings       → Detailed holdings view
/stocks         → Stock search
/stocks/:symbol → Technical analysis for a stock
/transactions   → Transaction history
/ai             → AI Assessment (chat, analysis, rebalancing tabs)
```

### Mobile Responsiveness
- Sidebar collapses to hamburger menu on screens < `lg` (1024px)
- Mobile top bar with menu toggle
- Touch-friendly buttons and spacing
- "Import Holdings" button opens a paste modal (works around Android file picker limitations on HTTP)

### API Key Handling on Frontend
- **Kite not configured**: Shows "Zerodha not configured" badge (disabled, grey) instead of "Connect" button
- **AI not configured**: Shows error message on analysis/chat/rebalancing pages with "Set ANTHROPIC_API_KEY" guidance
- **AI chat error**: SSE `event: error` displayed inline in chat as a warning message

---

## Configuration Reference

### Environment Variables
| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_HOST` | No | `localhost` | MySQL host |
| `DB_PORT` | No | `3306` | MySQL port |
| `DB_NAME` | No | `portfolio` | Database name |
| `DB_USER` | No | `portfolio_user` | Database username |
| `DB_PASSWORD` | No | `portfolio123` | Database password |
| `JWT_SECRET` | No | (built-in dev key) | JWT signing secret (change in production) |
| `KITE_API_KEY` | No | (empty) | Zerodha Kite API key |
| `KITE_API_SECRET` | No | (empty) | Zerodha Kite API secret |
| `ANTHROPIC_API_KEY` | No | (empty) | Claude AI API key |
| `FRONTEND_URL` | No | `http://localhost:5173` | Frontend URL for OAuth redirects |

### Caching
- `portfolioAnalysis` cache: In-memory `ConcurrentMapCache`, evicted on holding add/remove/sync/upload
- React Query: 30-second stale time, auto-refetch every 30 seconds on dashboard

### Scheduled Tasks
| Task | Interval | Description |
|------|----------|-------------|
| Holdings auto-sync | 15 minutes | Syncs Kite holdings for all connected users |
| Live price poller | Disabled | Requires paid Kite plan; was 5-second interval |

---

## Running the Application

### Prerequisites
- Java 23+
- Maven 3.9+
- MySQL 8+
- Node.js 20.19+ (or use `/opt/homebrew/bin/node` if system node is older)

### Database Setup
```sql
CREATE DATABASE portfolio;
CREATE USER 'portfolio_user'@'localhost' IDENTIFIED BY 'portfolio123';
GRANT ALL PRIVILEGES ON portfolio.* TO 'portfolio_user'@'localhost';
FLUSH PRIVILEGES;
```
Tables are auto-created by Hibernate (`ddl-auto: update`).

### Backend
```bash
cd portfolio-tracker

# Without API keys (CSV upload + manual data entry only)
mvn spring-boot:run

# With API keys
KITE_API_KEY=xxx KITE_API_SECRET=yyy ANTHROPIC_API_KEY=sk-ant-xxx mvn spring-boot:run
```
Backend starts at `http://localhost:8080`. Swagger UI at `http://localhost:8080/swagger-ui.html`.

### Frontend
```bash
cd portfolio-tracker-frontend
npm install
npm run dev -- --host 0.0.0.0 --port 5173
```
Frontend starts at `http://localhost:5173`. Vite proxies `/api/*` to `http://localhost:8080`.

### Default Admin User
Register via the UI or API:
```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@portfolio.com","password":"admin123","firstName":"Admin","lastName":"User"}'
```

---

## Project Structure

```
portfolio-tracker/                          # Spring Boot Backend
  src/main/java/com/portfolio/
    PortfolioApplication.java               # Main entry point
    config/SecurityConfig/
      SecurityConfig.java                   # Spring Security filter chain, CORS
      JwtTokenUtil.java                     # JWT generation/validation (JJWT)
      JwtTokenFilter.java                   # Extract & validate JWT from requests
      PasswordEncoderConfig.java            # BCrypt encoder (separate to avoid circular deps)
      WebSocketConfig.java                  # STOMP + SockJS configuration
      CacheConfig.java                      # In-memory cache manager
      AsyncConfig.java                      # Thread pool for async operations
      OpenApiConfig.java                    # Swagger/OpenAPI documentation config
    config/
      KiteConfig.java                       # Kite API key/secret properties
      AnthropicConfig.java                  # Claude API key/model/token config
    entity/
      User.java                             # User entity with Kite token storage
      Portfolio.java                        # Portfolio with holdings/transactions
      Stock.java                            # Stock master data
      PortfolioHolding.java                 # Holding linking portfolio <-> stock
      Transaction.java                      # Buy/sell transaction record
      StockPriceHistory.java                # Historical OHLCV data
      Alert.java                            # Price/portfolio alerts
      UserRole.java                         # Enum: USER, ADMIN
      TransactionType.java                  # Enum: BUY, SELL
    repository/
      UserRepository.java                   # findByEmail, findByKiteAccessTokenIsNotNull
      PortfolioRepository.java              # findByIdWithHoldings (fetch join)
      StockRepository.java                  # findBySymbol, searchBySymbolOrName
      PortfolioHoldingRepository.java       # findByPortfolioIdAndStockId, deleteByPortfolioId
      TransactionRepository.java            # findByPortfolioIdOrderByTransactionDateDesc
      StockPriceHistoryRepository.java      # Historical price queries
    service/
      UserService.java                      # Registration, authentication, UserDetailsService
      PortfolioService.java                 # Portfolio CRUD, metrics calculation
      StockService.java                     # Search, prices, technical indicators (SMA/RSI/MACD)
      TransactionService.java               # Execute transactions, update holdings
      KiteClient.java                       # Zerodha API client (OAuth, holdings, quotes)
      HoldingsSyncService.java              # Auto-sync holdings every 15 min
      AIAnalysisService.java                # Claude API calls (analysis, chat, rebalancing)
      CsvUploadService.java                 # Parse CSV/XLSX, create holdings in bulk
      WebSocketService.java                 # Broadcast price updates to clients
    controller/
      AuthController.java                   # POST /api/auth/register, /login
      PortfolioController.java              # Portfolio CRUD + CSV upload
      StockController.java                  # Stock search + technical analysis
      TransactionController.java            # Execute + list transactions
      AIController.java                     # AI analysis, chat (SSE), rebalancing
      KiteAuthController.java               # Zerodha OAuth + holdings sync
    dto/
      request/                              # Incoming DTOs (validated with @Valid)
      response/                             # Outgoing DTOs (built with @Builder)
      mapper/                               # Entity <-> DTO mappers (static methods)
      kite/                                 # KiteHolding, KiteSession DTOs
    exception/
      GlobalExceptionHandler.java           # @ControllerAdvice error handling
      ErrorResponse.java                    # Standard error response body
      ResourceNotFoundException.java        # 404 exception
      BusinessException.java                # 400 exception
      AuthenticationException.java          # 401 exception
  src/main/resources/
    application.yml                         # All configuration with env variable defaults

portfolio-tracker-frontend/                 # React Frontend
  src/
    main.tsx                                # React entry point
    App.tsx                                 # Routes + QueryClient + AuthGuard
    index.css                               # TailwindCSS + base styles
    api/
      client.ts                             # Axios instance with JWT interceptor
      auth.ts                               # login(), register()
      portfolio.ts                          # CRUD, analysis, transactions, CSV upload
      ai.ts                                 # AI status, analyze, rebalance, chat (SSE)
      kite.ts                               # Kite status, connect, sync, login URL
    store/
      authStore.ts                          # Zustand: token, user, login/logout
      portfolioStore.ts                     # Zustand: portfolio data, live price updates
    hooks/
      useWebSocket.ts                       # STOMP connection to /ws, subscribes /topic/prices
    pages/
      Login.tsx                             # Login/Register form
      Dashboard.tsx                         # Portfolio overview + CSV upload + Kite sync
      Holdings.tsx                          # Holdings table
      Transactions.tsx                      # Transaction history
      StockAnalysis.tsx                     # Technical analysis view
      AIAssessment.tsx                      # AI chat + analysis + rebalancing (tabbed)
    components/
      layout/
        AppLayout.tsx                       # Main layout with sidebar + mobile header
        Sidebar.tsx                         # Navigation (collapsible on mobile)
      portfolio/
        MetricsBar.tsx                      # Total value, P&L, holdings count, sectors
        HoldingsTable.tsx                   # Sortable holdings table
        SectorChart.tsx                     # Recharts sector allocation chart
      ai/
        AIChat.tsx                          # SSE streaming chat with Claude
        PortfolioHealthCard.tsx             # Health score + risk profile display
        RecommendationGrid.tsx              # Stock recommendation cards
        RebalancingPanel.tsx                # Rebalancing suggestion list
    types/
      index.ts                              # All TypeScript interfaces
  vite.config.ts                            # Vite config: proxy /api->:8080, global shim
  index.html                                # HTML shell with global=globalThis polyfill
```

---

## Known Issues & Workarounds

1. **sockjs-client + Vite**: `global is not defined` error in browser. Fixed with `define: { global: 'globalThis' }` in `vite.config.ts` and `<script>var global = globalThis;</script>` in `index.html`.

2. **Lombok + Java 23**: Requires `annotationProcessorPaths` in maven-compiler-plugin and `<scope>provided</scope>` on the Lombok dependency. Without this, annotation processors aren't discovered on Java 23+.

3. **Android HTTP file picker**: On non-HTTPS sites, Android Chrome restricts file input to camera/media only. The app provides a "Paste CSV" modal as a workaround for mobile users.

4. **CORS with credentials**: Spring rejects `allowedOrigins("*")` when `allowCredentials(true)`. Fixed by using `allowedOriginPatterns("*")` instead.

5. **Kite access tokens expire daily**: Users need to re-authenticate with Zerodha each trading day. The auto-sync will silently fail until the user reconnects.
