# CochaVive — Backend API

> A Spring Boot REST API to manage public events in Cochabamba. Clean, fast, and built to scale (eventually).

---

## Table of Contents

- [About the Project](#about-the-project)
- [Tech Stack](#tech-stack)
- [Requirements](#requirements)
- [Getting Started](#getting-started)
- [Environment Variables](#environment-variables)
  - [IntelliJ IDEA](#-intellij-idea)
  - [VS Code](#-vs-code)
  - [CLI (Terminal)](#-cli-terminal)
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
| **Cloudinary** *(coming soon)* | Cloud image storage for event photos |
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

> **Where do I get the Cloudinary credentials?**
> Contact the **CochaVive** team. See [Contact](#contact) below.

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
        "CLOUDINARY_API_SECRET": "your_api_secret"
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
mvnw.cmd spring-boot:run
```

---

## Project Structure

```
src/main/java/cocha/vive/backend/
│
├── BackendApplication.java         ← Entry point. The app starts here. Don't touch it.
│
├── config/
│   └── SwaggerConfig.java          ← OpenAPI/Swagger UI setup and API metadata.
│
├── controller/
│   └── CategoryController.java     ← HTTP layer. Receives requests, returns responses.
│                                     One controller per entity (Category, Event, User...).
│                                     No business logic lives here.
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
│   ├── UserRole.java               ← Enum for user roles (ADMIN, USER, etc.).
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

This project is developed by **CochaVive**.

For environment credentials (Cloudinary keys, shared DB access, etc.), reach out to the team:

📧 **fourElements418@gmail.com**

---

*Built with way too much caffeine and a genuine desire to make Cochabamba's event scene more accessible.*
