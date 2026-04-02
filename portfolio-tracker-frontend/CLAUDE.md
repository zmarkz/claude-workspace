# Portfolio Tracker Frontend

## Overview
A React single-page application for managing stock portfolios. Provides a dashboard with holdings visualization, transaction history, AI-powered portfolio analysis (chat with Claude), Zerodha Kite integration, and real-time price updates via WebSocket. Built with React 19, TypeScript, Vite, and TailwindCSS.

## Tech Stack
- Framework: React 19.2.4
- Language: TypeScript 5.9.3
- Build Tool: Vite 8.0.1
- Styling: TailwindCSS 4.2.2
- State Management: Zustand 5.0.12
- Data Fetching: TanStack React Query 5.96.1
- HTTP Client: Axios 1.14.0
- Routing: React Router DOM 7.13.2
- Charts: Recharts 3.8.1
- UI Components: Radix UI (dialog, dropdown, tabs, tooltip)
- Icons: Lucide React 1.7.0
- WebSocket: SockJS + STOMP.js
- Utilities: clsx, tailwind-merge

## How to Run Locally

### Without Docker (standalone)
```bash
cd ~/Documents/claude/portfolio-tracker-frontend
npm install
npm run dev
```
The app runs at `http://localhost:5173`. It proxies API requests to `http://localhost:8080` (the backend must be running).

### With Docker (via platform)
```bash
cd ~/Documents/claude/platform
docker compose up -d portfolio-tracker portfolio-tracker-frontend
```

## Project Structure
```
src/
├── main.tsx                    # React entry point
├── App.tsx                     # Root component with routing
├── index.css                   # Global styles (Tailwind imports)
├── api/                        # API client modules
│   ├── client.ts               # Axios instance (base URL, auth interceptor)
│   ├── auth.ts                 # Login/register API calls
│   ├── portfolio.ts            # Portfolio CRUD API calls
│   ├── stock.ts                # Stock data API calls
│   ├── kite.ts                 # Zerodha integration API calls
│   └── ai.ts                   # AI analysis API calls
├── components/
│   ├── layout/
│   │   ├── AppLayout.tsx       # Main layout with sidebar
│   │   └── Sidebar.tsx         # Navigation sidebar
│   ├── portfolio/
│   │   ├── HoldingsTable.tsx   # Holdings display table
│   │   ├── MetricsBar.tsx      # Portfolio summary metrics
│   │   └── SectorChart.tsx     # Sector allocation chart
│   └── ai/
│       ├── AIChat.tsx          # Chat interface with Claude
│       ├── RecommendationGrid.tsx
│       ├── RebalancingPanel.tsx
│       └── PortfolioHealthCard.tsx
├── pages/
│   ├── Login.tsx               # Authentication page
│   ├── Dashboard.tsx           # Main dashboard
│   ├── Holdings.tsx            # Holdings management
│   ├── Transactions.tsx        # Transaction history
│   ├── StockAnalysis.tsx       # Individual stock analysis
│   └── AIAssessment.tsx        # Full AI portfolio assessment
├── store/
│   ├── authStore.ts            # Auth state (token, user, login/logout)
│   └── portfolioStore.ts      # Portfolio state
├── hooks/
│   └── useWebSocket.ts        # WebSocket connection hook (STOMP)
├── types/
│   └── index.ts               # Shared TypeScript type definitions
└── assets/                    # Static assets
```

## Database
This is a frontend application — no direct database access. All data comes from the Portfolio Tracker API.

## Environment Variables

| Variable | Required | Default | Description |
|----------|----------|---------|-------------|
| `VITE_API_URL` | No | `http://localhost:8080` | Backend API base URL |

## Key Patterns & Conventions
- **API Client**: Centralized Axios instance in `api/client.ts` with auth token interceptor.
- **State Management**: Zustand for global state (auth, portfolio selection). React Query for server state.
- **Routing**: React Router v7 with protected routes.
- **Styling**: TailwindCSS utility classes. Radix UI for accessible primitives.
- **Component Naming**: PascalCase. Group by feature domain (`portfolio/`, `ai/`, `layout/`).
- **Pages vs Components**: Pages in `pages/` (route-level). Components in `components/` (reusable).

## Integration Points
- **Backend** (`portfolio-tracker`): All data comes from this API. Must be running.
- **WebSocket**: Real-time updates via STOMP over SockJS at backend's `/ws` endpoint.

## Common Tasks

### Add a new page
1. Create page component in `src/pages/{PageName}.tsx`
2. Add route in `src/App.tsx`
3. Add nav link in `src/components/layout/Sidebar.tsx`
4. Update this CLAUDE.md

### Add a new component
1. Create in `src/components/{domain}/{ComponentName}.tsx`
2. Use TailwindCSS for styling, Radix UI for interactive elements

### Build for production
```bash
npm run build
# Output in dist/
```
