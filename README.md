# CochaVive — Backend API

> A Spring Boot REST API to manage public events in Cochabamba. Clean, fast, and built to scale (eventually).

---

## Table of Contents

- [About the Project](#about-the-project)
- [Tech Stack](#tech-stack)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Testing and Coverage (JaCoCo)](#testing-and-coverage-jacoco)
- [Environment Variables](#environment-variables)
  - [IntelliJ IDEA](#-intellij-idea)
  - [VS Code](#-vs-code)
  - [CLI (Terminal)](#-cli-terminal)
- [Authentication and Authorization](#authentication-and-authorization)
  - [Google Sign-In and Client ID Setup](#google-sign-in-and-client-id-setup)
  - [Testing With OAuth Playground + HTTP Client](#testing-with-oauth-playground--http-client)
  - [Role Convention (Important)](#role-convention-important)
  - [How Endpoint Restrictions Work](#how-endpoint-restrictions-work)
- [Project Structure](#project-structure)
- [API Documentation](#api-documentation)
- [Contact](#contact)

---

## About the Project

CochaVive is a platform for discovering and publishing public events in Cochabamba, Bolivia. This repository contains the **backend** — a RESTful API built with Spring Boot that handles events, users, categories, and soon, image uploads via Cloudinary.

---

## Tech Stack

| Technology | Purpose |
|---|---|
| **Spring Boot 4.0.3** | Core framework — web, dependency injection, everything |
| **Spring Data JPA** | Database access layer — repositories, queries |
| **Hibernate** | ORM under the hood — maps Java classes to DB tables automatically |
| **PostgreSQL** | Relational database, currently powered by Neon (It's awesome) |
| **Lombok** | Kills boilerplate — no more writing getters, setters, constructors by hand |
| **Spring Security** | Authentication and authorization for protected endpoints |
| **JWT (JJWT)** | Stateless token generation and validation |
| **Google API Client** | Verifies Google ID token on social login |
| **Cloudinary** | Cloud image storage for event photos |
| **SpringDoc OpenAPI (Swagger UI)** | Auto-generated interactive API docs |
| **Spring DevTools** | Hot reload during development — saves your `Ctrl+C / mvn run` sanity |

---

## Requirements

Make sure you have these installed before doing anything else:

| Tool | Version |
|---|---|
| **Java (JDK)** | 21 or higher |
| **Maven** | 3.9+ (or just use the included `./mvnw` wrapper — no install needed) |
| **PostgreSQL** | 14+ |

To check your Java version:

```bash
java -version
```

You should see something like `openjdk 21.x.x`. If not, grab it from [adoptium.net](https://adoptium.net).

---

## Getting Started

**1. Clone the repository**

```bash
git clone <repository-url>
cd backend
```

**2. Create the PostgreSQL database**

```sql
CREATE DATABASE cochavive;
```

**3. Set the environment variables** *(see next section)*

**4. Run the app**

```bash
# Using the Maven wrapper (recommended — no Maven installation needed)
./mvnw spring-boot:run

# Or if you have Maven installed globally
mvn spring-boot:run
```

The API will be available at `http://localhost:8080`.

---

## Testing and Coverage (JaCoCo)

This project uses **JaCoCo** to generate code coverage from your tests.

Run tests + generate coverage report:

```bash
./mvnw test
```

If you want the report even when some tests fail:

```bash
./mvnw -Dmaven.test.failure.ignore=true test
```

Generated coverage files:

- HTML report: `target/site/jacoco/index.html`
- XML report: `target/site/jacoco/jacoco.xml`
- Exec data: `target/jacoco.exec`

Open the HTML report in your browser to inspect package/class/line coverage.

---

## Environment Variables

The app reads its sensitive configuration from environment variables — no hardcoded credentials, ever. You'll need to set the following:

| Variable | Description |
|---|---|
| `DB_LINK` | Full JDBC URL to your PostgreSQL database, e.g. `jdbc:postgresql://localhost:5432/cochavive` |
| `DB_USER` | Your database username |
| `DB_PASS` | Your database password |
| `CLOUDINARY_CLOUD_NAME` | Your Cloudinary cloud name |
| `CLOUDINARY_API_KEY` | Your Cloudinary API key |
| `CLOUDINARY_API_SECRET` | Your Cloudinary API secret |
| `JWT_SECRET_KEY` | Base64-encoded secret used to sign JWT tokens |
| `JWT_EXPIRATION_TIME` | JWT lifetime in milliseconds (example: `86400000` for 24h) |
| `GOOGLE_CLIENT_ID` | OAuth Client ID used to verify Google ID tokens |

> **Where do I get the shared credentials (Cloudinary, JWT secret policy, team DB, etc.)?**
> Contact the **CochaVive (Puy)** team. See [Contact](#contact) below.

---

### 💡 IntelliJ IDEA

1. Open **Run → Edit Configurations...**
2. Select your Spring Boot run configuration (or create one if you haven't yet).
3. Find the **Environment variables** field and click the folder icon on the right.
4. Add each variable as a key-value pair:

```
DB_LINK=jdbc:postgresql://localhost:5432/cochavive
DB_USER=your_username
DB_PASS=your_password
CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
JWT_SECRET_KEY=your_base64_secret
JWT_EXPIRATION_TIME=86400000
GOOGLE_CLIENT_ID=your_google_client_id
```

5. Click **OK** and run. Done.

---

### 💡 VS Code

You need the [Spring Boot Dashboard](https://marketplace.visualstudio.com/items?itemName=vscjava.vscode-spring-boot-dashboard) extension (if you don't have it, install it).

**Option A — `.env` via launch configuration**

Create or edit `.vscode/launch.json` in the project root:

```json
{
  "version": "0.2.0",
  "configurations": [
    {
      "type": "java",
      "name": "CochaVive Backend",
      "request": "launch",
      "mainClass": "cocha.vive.backend.BackendApplication",
      "projectName": "backend",
      "env": {
        "DB_LINK": "jdbc:postgresql://localhost:5432/cochavive",
        "DB_USER": "your_username",
        "DB_PASS": "your_password",
        "CLOUDINARY_CLOUD_NAME": "your_cloud_name",
        "CLOUDINARY_API_KEY": "your_api_key",
        "CLOUDINARY_API_SECRET": "your_api_secret",
        "JWT_SECRET_KEY": "your_base64_secret",
        "JWT_EXPIRATION_TIME": "86400000",
        "GOOGLE_CLIENT_ID": "your_google_client_id"
      }
    }
  ]
}
```

> **Important:** Add `.vscode/launch.json` to your `.gitignore` if it contains real credentials.

---

### 💡 CLI (Terminal)

**Linux / macOS — export for the current session:**

```bash
export DB_LINK=jdbc:postgresql://localhost:5432/cochavive
export DB_USER=your_username
export DB_PASS=your_password
export CLOUDINARY_CLOUD_NAME=your_cloud_name
export CLOUDINARY_API_KEY=your_api_key
export CLOUDINARY_API_SECRET=your_api_secret
export JWT_SECRET_KEY=your_base64_secret
export JWT_EXPIRATION_TIME=86400000
export GOOGLE_CLIENT_ID=your_google_client_id

./mvnw spring-boot:run
```

**Or inline (one-liner):**

```bash
DB_LINK=jdbc:postgresql://localhost:5432/cochavive \
DB_USER=your_username \
DB_PASS=your_password \
CLOUDINARY_CLOUD_NAME=your_cloud_name \
CLOUDINARY_API_KEY=your_api_key \
CLOUDINARY_API_SECRET=your_api_secret \
JWT_SECRET_KEY=your_base64_secret \
JWT_EXPIRATION_TIME=86400000 \
GOOGLE_CLIENT_ID=your_google_client_id \
./mvnw spring-boot:run
```

**Windows (Command Prompt):**

```cmd
set DB_LINK=jdbc:postgresql://localhost:5432/cochavive
set DB_USER=your_username
set DB_PASS=your_password
set CLOUDINARY_CLOUD_NAME=your_cloud_name
set CLOUDINARY_API_KEY=your_api_key
set CLOUDINARY_API_SECRET=your_api_secret
set JWT_SECRET_KEY=your_base64_secret
set JWT_EXPIRATION_TIME=86400000
set GOOGLE_CLIENT_ID=your_google_client_id
mvnw.cmd spring-boot:run
```

---

## Authentication and Authorization

This backend uses a stateless security flow:

1. Frontend gets a Google ID token.
2. Frontend sends it to `POST /api/auth/google`.
3. Backend verifies it using `GOOGLE_CLIENT_ID`.
4. Backend creates/fetches the user and returns an internal JWT.
5. Frontend sends that JWT in `Authorization: Bearer <token>` for protected endpoints.

### Google Sign-In and Client ID Setup

To get `GOOGLE_CLIENT_ID`:

1. Go to [Google Cloud Console](https://console.cloud.google.com/).
2. Create a project (or use an existing one).
3. Open **APIs & Services → OAuth consent screen** and configure the app.
4. Open **APIs & Services → Credentials → Create Credentials → OAuth client ID**.
5. Choose **Web application**.
6. Add authorized origins used by the frontend (for local dev, usually `http://localhost:4200`).
7. Copy the generated **Client ID** and set it as `GOOGLE_CLIENT_ID` in your environment.

### Testing With OAuth Playground + HTTP Client

If you want to test backend auth without frontend, this is the fastest workflow.

#### 1) Get a Google ID token from OAuth Playground

1. Open [Google OAuth 2.0 Playground](https://developers.google.com/oauthplayground/).
2. Click the ⚙️ gear icon (top-right).
3. Enable **Use your own OAuth credentials** (server-side style flow).
4. Paste your OAuth **Client ID** and **Client Secret**.
5. In Step 1, use scopes:
   - `openid`
   - `email`
   - `profile`
6. Click **Authorize APIs** and complete Google sign-in.
7. Click **Exchange authorization code for tokens**.
8. Copy `id_token` from the response.

Important:
- The `aud` claim inside that `id_token` must match your backend `GOOGLE_CLIENT_ID`.
- If they do not match, `/api/auth/google` will reject the token.

#### 2) Exchange Google token for internal JWT

Use any HTTP client (Postman, Insomnia, VS Code REST Client, IntelliJ HTTP Client, curl).

```http
POST http://localhost:8080/api/auth/google
Content-Type: application/json

{
  "token": "<google_id_token>"
}
```

Expected response:

```json
{
  "token": "<internal_jwt>",
  "requiresOnboarding": false
}
```

#### 3) Call protected endpoints with Bearer token

```http
POST http://localhost:8080/api/events
Authorization: Bearer <internal_jwt>
Content-Type: multipart/form-data
```

For JSON-only protected endpoints, same header applies:

```http
Authorization: Bearer <internal_jwt>
```

#### 4) Quick auth troubleshooting checklist

- `401 Unauthorized` on `/api/auth/google`:
  - `GOOGLE_CLIENT_ID` is missing or wrong.
  - You are sending `access_token` instead of `id_token`.
- `403 Forbidden` on protected endpoint:
  - User role in DB does not have required authority.
  - Role value is missing `ROLE_` prefix (for example `ADMIN` instead of `ROLE_ADMIN`).
- JWT works in one request and fails later:
  - Check `JWT_EXPIRATION_TIME` and server clock sync.

### Role Convention (Important)

Roles in this project are stored as plain strings in the `users.role` field and converted to `GrantedAuthority` directly.

- Every role value **must start with `ROLE_`**.
- Valid examples: `ROLE_USER`, `ROLE_ADMIN`.
- Invalid examples: `USER`, `ADMIN`.

Why? Because Spring checks `hasRole("ADMIN")` against authority `ROLE_ADMIN` internally. If the prefix is missing, authorization will fail even if the user "looks" admin in the DB.

### How Endpoint Restrictions Work

There are two levels of restrictions in this codebase:

1. Global URL rules in `SecurityConfig`:
  - `/api/auth/**` is public.
  - `GET /api/events/**` and `GET /api/categories/**` are public.
  - `/api/admin/**` requires admin role.
  - Any other endpoint requires authentication.

2. Method-level rules with `@PreAuthorize` in controllers:
  - `@PreAuthorize("hasAnyRole('USER')")` for creating events.
  - `@PreAuthorize("hasAnyRole('ADMIN')")` for creating categories.

When adding a new endpoint, pick one of these patterns:

- Public endpoint: add explicit `permitAll()` matcher.
- Any logged-in user: keep it under `.anyRequest().authenticated()`.
- Role-restricted endpoint: add `@PreAuthorize("hasRole('ADMIN')")` or `@PreAuthorize("hasAnyRole('USER','ADMIN')")`.

Quick example:

```java
@PostMapping("/admin/reports")
@PreAuthorize("hasRole('ADMIN')")
public ResponseEntity<?> createReport() { ... }
```

---

## Project Structure

```
src/main/java/cocha/vive/backend/
│
├── BackendApplication.java         ← Entry point. The app starts here. Don't touch it.
│
├── config/
│   ├── SwaggerConfig.java          ← OpenAPI/Swagger UI setup and API metadata.
│   ├── CloudinaryConfig.java       ← Cloudinary client configuration.
│   └── CorsConfig.java             ← CORS setup shared by the API.
│
├── auth/
│   ├── SecurityConfig.java         ← Security filter chain, CORS, URL-level access rules.
│   ├── JwtAuthenticationFilter.java← Reads Bearer token and sets SecurityContext.
│   ├── JwtService.java             ← Generates and validates JWT tokens.
│   ├── AuthController.java         ← Google token login endpoint (`/api/auth/google`).
│   ├── AuthResponse.java           ← Authentication response DTO.
│   └── TokenDto.java               ← Incoming Google token payload.
│
├── controller/
│   └── CategoryController.java     ← HTTP layer. Receives requests, returns responses.
│                                     One controller per entity (Category, Event, User...).
│                                     No business logic lives here.
│                                     Also includes auth-protected actions via `@PreAuthorize`.
│
├── exception/
│   ├── GlobalExceptionHandler.java ← Catches all unhandled exceptions across the app
│   │                                 and returns clean, consistent error responses.
│   └── ResourceNotFoundException.java ← Thrown when a requested resource doesn't exist (404).
│
├── model/
│   ├── Category.java               ← JPA entities — each file maps to a database table.
│   ├── Event.java
│   ├── EventStatus.java            ← Enum for event states (APPROVED, PENDING, etc.).
│   ├── User.java
│   │
│   └── dto/
│       ├── CategoryCreateDTO.java  ← Data Transfer Objects. Used as request/response bodies.
│       ├── ErrorResponseDTO.java     DTOs decouple what the API exposes from the DB schema.
│       ├── EventCreateDTO.java
│       └── UserCreateDTO.java
│
├── repository/
│   ├── CategoryRepository.java     ← Data access layer. Extends JpaRepository for free
│   ├── EventRepository.java          CRUD operations. Custom queries go here too.
│   └── UserRepository.java
│
└── service/
  └── CategoryService.java        ← Business logic layer. Controllers call services.
                                      Services call repositories. Keep it clean.
                    Also includes user/event services used by JWT auth flow.
```

### The flow of a request

```
HTTP Request
    ↓
Controller        (validates input, delegates work)
    ↓
Service           (applies business rules)
    ↓
Repository        (queries the database via Hibernate/JPA)
    ↓
PostgreSQL
```

---

## API Documentation

Once the app is running, Swagger UI is available at:

```
http://localhost:8080/swagger-ui/index.html
```

It lists every endpoint, their expected inputs, possible responses, and lets you test them directly from the browser. No Postman required to get started.

---

## Contact

This project is developed by **CochaVive (Puy)**.

For environment credentials (Cloudinary keys, shared DB access, etc.), reach out to the team:

📧 **fourElements418@gmail.com**

---

*Built with way too much caffeine and a genuine desire to make Cochabamba's event scene more accessible.*
