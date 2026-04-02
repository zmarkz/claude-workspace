# Portfolio Tracker API

## Overview
A Spring Boot REST API for managing stock portfolios. Supports multi-portfolio management, transaction tracking, CSV/XLSX upload, real-time price updates via WebSocket, AI-powered portfolio analysis using Claude (Anthropic), and Zerodha Kite integration for live holdings sync.

## Tech Stack
- Language: Java 23
- Framework: Spring Boot 3.2.0
- Build Tool: Maven
- Database: MySQL 8.0 via Spring Data JPA / Hibernate
- Authentication: JWT (JJWT 0.11.5)
- API Docs: SpringDoc OpenAPI 2.3.0
- AI: Anthropic Claude Sonnet (via Anthropic Java SDK)
- Broker: Zerodha Kite Connect API
- Other: Lombok, Jackson, Apache POI (Excel), Spring WebSocket (STOMP), Spring Cache

## How to Run Locally

### Without Docker (standalone)
```bash
# Prerequisites: Java 23, Maven, MySQL 8 running on localhost:3306
cd ~/Documents/claude/portfolio-tracker

# Create the database
mysql -u root -e "CREATE DATABASE IF NOT EXISTS portfolio_tracker;"

# Set environment variables (or copy .env.example to .env and source it)
export DB_HOST=localhost DB_PORT=3306 DB_NAME=portfolio_tracker
export DB_USER=root DB_PASSWORD=yourpassword
export JWT_SECRET=your-32-char-minimum-secret-key-here
export ANTHROPIC_API_KEY=sk-ant-...
export FRONTEND_URL=http://localhost:5173

# Run
mvn spring-boot:run
```

### With Docker (via platform)
```bash
cd ~/Documents/claude/platform
docker compose up -d mysql portfolio-tracker
```

The API will be available at `http://localhost:8080`.
Swagger UI: `http://localhost:8080/swagger-ui.html`

## Project Structure
```
src/main/java/com/portfolio/
├── PortfolioApplication.java     # Spring Boot entry point
├── config/                        # Configuration classes
│   ├── AnthropicConfig.java       # Claude AI client config
│   ├── KiteConfig.java            # Zerodha Kite client config
│   └── SecurityConfig/            # Spring Security + JWT
│       ├── SecurityConfig.java    # Security filter chain
│       ├── JwtTokenUtil.java      # JWT generation/validation
│       └── JwtTokenFilter.java    # Request filter for JWT
├── controller/                    # REST endpoints
│   ├── AuthController.java        # POST /api/auth/register, /login
│   ├── PortfolioController.java   # CRUD /api/portfolios
│   ├── StockController.java       # GET /api/stocks
│   ├── TransactionController.java # CRUD /api/transactions
│   ├── AIController.java          # POST /api/ai/analyze, /chat
│   └── KiteAuthController.java    # Zerodha OAuth flow
├── entity/                        # JPA entities
│   ├── User.java
│   ├── Portfolio.java
│   ├── Stock.java
│   ├── PortfolioHolding.java
│   ├── Transaction.java
│   ├── StockPriceHistory.java
│   └── Alert.java
├── repository/                    # Spring Data JPA repositories
├── service/                       # Business logic
│   ├── AIAnalysisService.java     # Claude AI integration
│   ├── KiteClient.java            # Zerodha API client
│   ├── HoldingsSyncService.java   # Sync holdings from Kite
│   ├── CsvUploadService.java      # CSV/XLSX import
│   └── WebSocketService.java      # Real-time push notifications
├── dto/                           # Request/Response DTOs + mappers
└── exception/                     # Global error handling
```

## Database
- **Database name**: `portfolio_tracker`
- **ORM**: Spring Data JPA / Hibernate (auto-DDL via `spring.jpa.hibernate.ddl-auto`)

### Key Entities and Relationships
```
User (1) ──── (N) Portfolio
Portfolio (1) ──── (N) PortfolioHolding
Portfolio (1) ──── (N) Transaction
Stock (1) ──── (N) PortfolioHolding
Stock (1) ──── (N) StockPriceHistory
User (1) ──── (N) Alert
```

### Enums
- `TransactionType`: BUY, SELL
- `UserRole`: USER, ADMIN

## API Endpoints

### Authentication
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/register` | Register new user |
| POST | `/api/auth/login` | Login, returns JWT token |

### Portfolios
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/portfolios` | List user's portfolios |
| POST | `/api/portfolios` | Create portfolio |
| GET | `/api/portfolios/{id}` | Get portfolio details |
| GET | `/api/portfolios/{id}/holdings` | Get portfolio holdings |

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

### AI Analysis
| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/ai/analyze` | Analyze portfolio with Claude |
| POST | `/api/ai/chat` | Chat with AI about portfolio |

### Kite (Zerodha)
| Method | Path | Description |
|--------|------|-------------|
| GET | `/api/kite/auth-url` | Get Kite OAuth URL |
| POST | `/api/kite/callback` | Handle OAuth callback |
| POST | `/api/kite/sync` | Sync holdings from Kite |

### WebSocket
- Endpoint: `/ws` (STOMP over SockJS)
- Topics: portfolio updates, price alerts

Swagger UI available at: `http://localhost:8080/swagger-ui.html`

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `DB_HOST` | Yes | `localhost` | MySQL host |
| `DB_PORT` | Yes | `3306` | MySQL port |
| `DB_NAME` | Yes | `portfolio_tracker` | Database name |
| `DB_USER` | Yes | — | MySQL username |
| `DB_PASSWORD` | Yes | — | MySQL password |
| `JWT_SECRET` | Yes | — | JWT signing key (min 32 chars) |
| `ANTHROPIC_API_KEY` | Yes | — | Claude API key |
| `ANTHROPIC_MODEL` | No | `claude-sonnet-4-6` | Claude model |
| `KITE_API_KEY` | No | — | Zerodha API key |
| `KITE_API_SECRET` | No | — | Zerodha API secret |
| `FRONTEND_URL` | Yes | `http://localhost:5173` | CORS origin |

## Key Patterns & Conventions
- **Error handling**: Global exception handler via `@ControllerAdvice` in `GlobalExceptionHandler.java`. Custom exceptions: `BusinessException`, `AuthenticationException`, `ResourceNotFoundException`.
- **Authentication**: JWT tokens in `Authorization: Bearer <token>` header. 24-hour expiry. `JwtTokenFilter` extracts and validates on every request.
- **DTOs**: All API inputs/outputs use DTOs (never expose entities directly). Mappers in `dto/mapper/`.
- **Naming**: Controllers are `{Entity}Controller`, Services are `{Entity}Service`, Repositories are `{Entity}Repository`.
- **Config**: All external config in `application.yml`, overridden by environment variables.
- **Testing**: JUnit 5 + Spring Boot Test. Run with `mvn test`.

## Integration Points
- **Frontend** (`portfolio-tracker-frontend`): Calls this API on port 8080. CORS configured for `FRONTEND_URL`.
- **Zerodha Kite**: External API for live market data and holdings sync.
- **Anthropic Claude**: External API for AI-powered portfolio analysis and chat.
- **MCP Gateway** (future): Could use MCP tools for enhanced market research.

## Common Tasks

### Add a new API endpoint
1. Create/update entity in `entity/`
2. Create/update repository in `repository/`
3. Add business logic in `service/`
4. Create request/response DTOs in `dto/`
5. Create mapper in `dto/mapper/`
6. Add controller method in `controller/`
7. Update this CLAUDE.md with the new endpoint

### Add a new entity
1. Create `@Entity` class in `entity/`
2. Create `JpaRepository` interface in `repository/`
3. Run the app — Hibernate auto-creates the table
4. Update the entity relationship diagram above

### Run tests
```bash
mvn test
```

### Build for production
```bash
mvn clean package -DskipTests
java -jar target/portfolio-tracker-*.jar
```
