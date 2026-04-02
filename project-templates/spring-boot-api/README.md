# Spring Boot API Template

A production-ready Spring Boot 3.2 API template with JWT authentication, Spring Security, JPA, and OpenAPI documentation.

## Features

- Spring Boot 3.2 with Java 23
- JWT-based authentication with JJWT
- Spring Security with stateless session management
- CORS configuration
- JPA/Hibernate with MySQL
- Lombok for reduced boilerplate
- OpenAPI 3 / Swagger UI documentation
- Global exception handling
- Docker multi-stage build support
- Comprehensive validation

## Quick Start

### 1. Clone and Setup

```bash
git clone <repository-url>
cd {{APP_NAME}}
cp .env.example .env
```

### 2. Configure Environment

Edit `.env` with your settings:
- `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD` - MySQL connection details
- `JWT_SECRET` - A secure random string (minimum 32 characters for HS512)
- `FRONTEND_URL` - Your frontend application URL

### 3. Database Setup

```bash
# Create MySQL database
mysql -u root -p -e "CREATE DATABASE {{APP_NAME}}_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
```

The application will automatically create tables on startup using Hibernate's `ddl-auto: update`.

### 4. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

The API will start on `http://localhost:8080`

## API Endpoints

### Health Check
- **GET** `/health` - Returns `{"status":"ok"}`

### Authentication
- **POST** `/api/auth/register` - Register new user
- **POST** `/api/auth/login` - Login and receive JWT token

### Protected Endpoints
All other endpoints require a valid JWT token in the `Authorization` header:
```
Authorization: Bearer <your-jwt-token>
```

## Documentation

### Swagger UI
Visit `http://localhost:8080/swagger-ui.html` for interactive API documentation.

### API Docs
JSON API specification available at `http://localhost:8080/v3/api-docs`

## Project Structure

```
src/main/
├── java/com/template/
│   ├── Application.java          # Spring Boot entry point
│   ├── config/
│   │   ├── SecurityConfig.java   # Spring Security configuration
│   │   ├── JwtTokenUtil.java     # JWT token generation/validation
│   ├── security/
│   │   └── JwtTokenFilter.java   # JWT extraction filter
│   ├── controller/
│   │   ├── HealthController.java
│   │   └── AuthController.java
│   ├── service/
│   │   └── UserService.java
│   ├── entity/
│   │   └── User.java
│   ├── repository/
│   │   └── UserRepository.java
│   ├── dto/
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   └── AuthResponse.java
│   └── exception/
│       ├── GlobalExceptionHandler.java
│       └── ErrorResponse.java
└── resources/
    └── application.yml           # Application configuration
```

## Building Docker Image

```bash
docker build -t {{APP_NAME}}:latest .
docker run -p 8080:8080 \
  -e DB_HOST=mysql \
  -e DB_PORT=3306 \
  -e DB_NAME={{APP_NAME}}_db \
  -e DB_USER=root \
  -e DB_PASSWORD=password \
  -e JWT_SECRET=your-secret-key-min-32-chars \
  {{APP_NAME}}:latest
```

## Testing

```bash
# Run tests
mvn test

# Run with coverage
mvn test jacoco:report
```

## Authentication Flow

1. **Register**: POST `/api/auth/register` with email and password
2. **Login**: POST `/api/auth/login` to receive JWT token
3. **Use Token**: Include token in `Authorization: Bearer <token>` header
4. **Token Expiry**: Tokens expire after 24 hours

## Placeholder Replacement Guide

Replace the following placeholders in your project:

- `{{APP_GROUP}}` - Your package group (e.g., `com.example`)
- `{{APP_NAME}}` - Your application name (e.g., `myapp`)

Update these in:
- `pom.xml` - groupId and artifactId
- `src/main/resources/application.yml` - database name and log levels
- `.env.example` - database name
- This README.md

## Configuration Details

### Spring Security
- Stateless session management (JWT-based)
- CORS enabled for frontend communication
- CSRF disabled for stateless API
- Public endpoints: `/api/auth/**`, `/health`, `/swagger-ui/**`, `/v3/api-docs/**`

### JWT Configuration
- Algorithm: HS512
- Expiration: 24 hours (configurable via `jwt.expiration`)
- Claims: email, role
- Secret: Retrieved from `JWT_SECRET` environment variable

### Database
- Driver: MySQL Connector/J
- Dialect: MySQL8Dialect
- DDL Mode: update (automatically alters schema on startup)
- Connection Pooling: HikariCP (default)

## Dependencies

- Spring Boot 3.2.0
- Spring Security
- Spring Data JPA
- MySQL Connector/J
- JJWT 0.11.5
- Lombok
- SpringDoc OpenAPI 2.3.0

## License

MIT License - See LICENSE file for details

## Support

For issues or questions, please refer to the documentation or create an issue in the repository.
