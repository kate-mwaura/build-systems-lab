# greeting-service

> Production-style Spring Boot microservice demonstrating modern backend engineering and DevOps practices.

![Java](https://img.shields.io/badge/Java-21-orange?logo=openjdk)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-brightgreen?logo=springboot)
![Maven](https://img.shields.io/badge/Maven-3.9.6-red?logo=apachemaven)
![Docker](https://img.shields.io/badge/Docker-multi--stage-blue?logo=docker)

---

## What This Project Is

`greeting-service` is a containerized Spring Boot microservice built to demonstrate how a production-oriented backend service is structured, configured, and shipped in a real engineering environment. The project goes beyond a basic "Hello World" it is intentionally architected using patterns and tooling that mirror what engineering teams use in production systems.

The project is built on a **Maven multi-module architecture**, where a parent POM centralizes all dependency versions, plugin versions, and build configuration, and a child module (`greeting-api`) inherits from it and contains the actual application. This separation ensures that as the system scales and more modules are added, version consistency and build standardization are maintained across the entire codebase without duplication.

Configuration is fully externalized and **profile-driven**. The application behaves differently depending on the environment it runs in development gets verbose console logging on port 9090, while production gets structured file-based logging with automatic rotation on port 9080. These environments are controlled through Spring profiles and Maven profiles, keeping environment-specific values out of source code entirely.

The application is **containerized using a multi-stage Docker build** that separates the build environment from the runtime environment, producing a lightweight production image. A Docker Compose setup orchestrates the full stack locally the API, SonarQube for code quality analysis, and Snyk for dependency vulnerability scanning making the entire environment reproducible with a single command.

Logging is handled through **Logback with profile-aware configuration**. In development, logs go to the console in a readable format. In production, logs are written to a rolling file appender that rotates based on size and time, with a retention policy to prevent disk saturation. This makes the application observable and ready for integration with centralized logging platforms like ELK or Grafana Loki.

Code quality is enforced through **JaCoCo** (test coverage with a 80% instruction coverage gate) and **SonarQube** (static analysis, code smells, maintainability scoring). Dependency vulnerabilities are scanned using **Snyk**. The project is structured to be CI/CD-ready, with the intent of plugging these quality gates directly into a pipeline.

---

## Architecture Overview

```
┌─────────────────────────────────────────────────────┐
│                  greeting-service                    │
│                   (Parent POM)                       │
│   centralizes: dependencies, plugins, profiles       │
└──────────────────────┬──────────────────────────────┘
                       │ inherits
          ┌────────────▼────────────┐
          │       greeting-api       │
          │    (Child Module)        │
          │  Spring Boot Application │
          └────────────┬────────────┘
                       │
         ┌─────────────▼──────────────┐
         │      Docker Container       │
         │  eclipse-temurin:21-jre     │
         │  prod profile active        │
         │  logs → /app/logs/          │
         └─────────────┬──────────────┘
                       │
        ┌──────────────▼───────────────┐
        │        Docker Compose         │
        │  greeting-api  → port 9080    │
        │  sonarqube     → port 9000    │
        │  snyk          → scanner      │
        └───────────────────────────────┘
```

The parent POM is the single source of truth for build configuration. The child module inherits everything and only defines what is specific to itself — its own dependencies, plugin executions, and deployment logic. Version numbers live in one place and one place only.

---

## Project Structure

```bash
greeting-service/
│
├── pom.xml                          # Parent POM — dependency + plugin management
├── Dockerfile                       # Multi-stage build (build → runtime)
├── docker-compose.yml               # Full stack orchestration
│
└── greeting-api/                    # Child module — the actual Spring Boot app
    ├── pom.xml                      # Child POM — inherits from parent
    └── src/
        ├── main/
        │   ├── java/com/example/greeting/
        │   │   ├── GreetingApplication.java        # Application entry point
        │   │   └── controller/
        │   │       └── GreetingController.java     # REST controller with SLF4J logging
        │   └── resources/
        │       ├── application.properties          # Base config, activates dev by default
        │       ├── application-dev.properties      # Dev environment — port 9090
        │       ├── application-prod.properties     # Prod environment — port 9080
        │       └── logback-spring.xml              # Profile-aware logging configuration
        └── test/
            └── java/com/example/greeting/
                └── GreetingApplicationTests.java   # Integration tests
```

---

## Tech Stack

| Technology | Version | Purpose |
|---|---|---|
| Java | 21 | Application runtime |
| Spring Boot | 4.0.6 | Application framework |
| Maven | 3.9.6 | Build automation and dependency management |
| SLF4J + Logback | managed by Boot BOM | Logging abstraction and implementation |
| JaCoCo | 0.8.11 | Test coverage reporting and enforcement |
| SonarQube | LTS Community | Static analysis and code quality |
| Snyk | latest | Dependency vulnerability scanning |
| Docker | multi-stage | Containerization |
| Docker Compose | 3.8 | Local stack orchestration |

---

## Getting Started

Make sure you have the following installed before running the project:

- **Java 21** — [Download](https://adoptium.net/)
- **Maven 3.9+** — [Download](https://maven.apache.org/download.cgi)
- **Docker** — [Download](https://www.docker.com/products/docker-desktop)

Clone the repository:

```bash
git clone https://github.com/Kate-mwaura/build-systems-lab.git
cd greeting-service
```

---

## Running the Application

### Local — Maven (dev profile, default)

Runs on port `9090` with console logging at DEBUG level for application code.

```bash
cd greeting-api
mvn spring-boot:run
```

### Local — Maven (prod profile)

Runs on port `9080` with file-based logging. Logs written to `greeting-api/logs/greeting-api.log`.

```bash
cd greeting-api
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

### Packaged JAR

```bash
# Build from root
mvn clean package

# Run with dev profile
java -jar greeting-api/target/greeting-api-1.0-SNAPSHOT.jar

# Run with prod profile
java -jar greeting-api/target/greeting-api-1.0-SNAPSHOT.jar --spring.profiles.active=prod
```

### Docker

Builds and runs the application in a container with the prod profile active.

```bash
# Build the image from the project root (Dockerfile lives here)
docker build -t greeting-api:latest .

# Run the container
docker run -p 9080:9080 greeting-api:latest
```

### Docker Compose

Spins up the full stack — the API, SonarQube, and Snyk — with a single command.

```bash
docker compose up --build
```

Services available after startup:

| Service | URL |
|---|---|
| greeting-api | http://localhost:9080 |
| SonarQube | http://localhost:9000 |

To stop:

```bash
docker compose down
```

---

## Environment Profiles

The application uses Spring profiles to manage environment-specific configuration. The active profile controls the port, logging behavior, and runtime configuration.

| Profile | Port | Log destination | Log level |
|---|---|---|---|
| `dev` | 9090 | Console | DEBUG (app), INFO (framework) |
| `prod` | 9080 | Rolling file `/app/logs/` | INFO (app), WARN (framework) |

The `dev` profile is active by default when running locally. In Docker and Docker Compose, the `prod` profile is injected via environment variable:

```yaml
environment:
  - SPRING_PROFILES_ACTIVE=prod
```

To override locally at runtime:

```bash
SPRING_PROFILES_ACTIVE=prod java -jar greeting-api.jar
```

---

## Logging

Logging is configured through `logback-spring.xml` using `<springProfile>` blocks, which allows a single configuration file to handle both environments cleanly.

**Development** — logs go to the console in a human-readable pattern. Application code logs at `DEBUG`, Spring framework logs at `INFO` to reduce noise.

**Production** — logs go to a `RollingFileAppender` at `/app/logs/greeting-api.log`. The file rotates based on size (10MB) and time, with a 30-day retention policy and a 1GB total size cap to prevent disk saturation. Only `WARN` and above are written to file.

Log rotation archive pattern:
```
logs/archive/application-{date}.{index}.log.gz
```

In a production deployment, the `/app/logs/` volume is mounted externally via Docker and can be consumed by a log shipper (Fluentd, Promtail) for centralized aggregation in ELK or Grafana Loki.

---

## Code Quality

### SonarQube + JaCoCo

JaCoCo is configured to run during the `test` phase and generate a coverage report. A coverage gate enforces a minimum of **80% instruction coverage** — the build fails if this threshold is not met.

To run the full analysis locally (requires SonarQube running on port 9000):

```bash
# Start SonarQube first
docker start sonarqube

# Run analysis from greeting-api directory
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=${SONAR_TOKEN}
```

Results are available at `http://localhost:9000` after the analysis completes.

### Snyk

Snyk is included in the Docker Compose stack as a dependency vulnerability scanner. It scans both the project dependencies and the container image for known CVEs.

To run a Snyk scan manually against the project:

```bash
docker compose run snyk snyk test --all-projects
```

To scan the container image:

```bash
docker compose run snyk snyk container test greeting-api:latest
```

> **Note:** Snyk requires a valid `SNYK_TOKEN` environment variable. Store this in a `.env` file and never commit it to version control.

```bash
# .env
SNYK_TOKEN=your_token_here
```

---

## API Reference

### GET `/greet`

Returns a greeting message for the provided name.

**Query Parameters:**

| Parameter | Type | Required | Default | Description |
|---|---|---|---|---|
| `name` | string | No | `World` | The name to greet |

**Example Request:**

```bash
curl "http://localhost:9090/greet?name=Thony"
```

**Example Response:**

```
Hello, Thony!
```

**Actuator Endpoints:**

| Endpoint | Description |
|---|---|
| `/actuator/health` | Application health status |
| `/actuator/info` | Application info |

---

## Configuration Reference

| Property | File | Default | Description |
|---|---|---|---|
| spring.application.name | application.properties | greeting-api | Application name used in logs and monitoring |
| spring.profiles.active | application.properties | dev | Active Spring profile |
| server.port | application-dev.properties | 9090 | Port for dev environment |
| server.port | application-prod.properties | 9080 | Port for prod environment |

Sensitive values such as tokens and credentials are injected through environment variables and are never hardcoded in configuration files or committed to version control.