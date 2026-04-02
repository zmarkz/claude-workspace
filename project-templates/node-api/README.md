# Node.js/TypeScript API Template

A production-ready Fastify-based REST API template with TypeScript, authentication, database ORM, and containerization.

## Features

- Fastify framework with OpenAPI/Swagger documentation
- TypeScript with strict mode enabled
- Authentication with JWT and bcryptjs
- PostgreSQL with Drizzle ORM
- Structured error handling
- CORS support
- Pino logging
- Docker multi-stage builds
- Environment configuration with Zod validation
- Health check endpoint

## Quick Start

### Prerequisites

- Node.js 20+
- PostgreSQL 12+
- Docker (optional)

### Setup

1. Clone and install dependencies:
```bash
npm install
```

2. Configure environment:
```bash
cp .env.example .env
# Edit .env with your database URL and JWT secret
```

3. Create database migrations:
```bash
npm run db:generate
npm run db:migrate
```

4. Start development server:
```bash
npm run dev
```

Server runs on http://localhost:3000 with Swagger docs at http://localhost:3000/docs

### Available Scripts

- `npm run dev` - Start development server with hot reload
- `npm run build` - Build TypeScript to JavaScript
- `npm start` - Run built application
- `npm test` - Run tests with Vitest
- `npm run db:generate` - Generate database migrations
- `npm run db:migrate` - Apply database migrations

## Project Structure

```
src/
├── config.ts           # Configuration management with Zod validation
├── index.ts            # Entry point
├── db/
│   ├── index.ts        # Database initialization
│   └── schema.ts       # Drizzle schema definitions
├── routes/
│   ├── health.ts       # Health check endpoint
│   └── auth.ts         # Authentication routes
├── middleware/
│   └── auth.ts         # JWT authentication middleware
└── utils/
    ├── logger.ts       # Pino logger setup
    └── errors.ts       # Custom error classes and handler
```

## Environment Variables

- `DATABASE_URL` - PostgreSQL connection string (required)
- `PORT` - Server port (default: 3000)
- `LOG_LEVEL` - Pino log level (default: info)
- `JWT_SECRET` - JWT signing secret, min 32 characters (required)
- `NODE_ENV` - Environment: development, production, test

## API Endpoints

### Health Check
- `GET /health` - Returns server status and timestamp

### Authentication
- `POST /auth/register` - Register new user
  ```json
  { "email": "user@example.com", "password": "securepassword" }
  ```
- `POST /auth/login` - Login user
  ```json
  { "email": "user@example.com", "password": "password" }
  ```

## Docker

Build and run with Docker:
```bash
docker build -t my-api .
docker run -p 3000:3000 --env-file .env my-api
```

## Error Handling

The API uses structured error responses:
```json
{
  "error": {
    "code": "VALIDATION_ERROR",
    "message": "Detailed error message"
  }
}
```

Error codes: `VALIDATION_ERROR`, `AUTHENTICATION_ERROR`, `AUTHORIZATION_ERROR`, `NOT_FOUND`, `CONFLICT`, `INTERNAL_SERVER_ERROR`

## Database

Drizzle ORM is configured for PostgreSQL with type-safe queries. Create migrations after schema changes:
```bash
npm run db:generate
npm run db:migrate
```

## License

MIT
