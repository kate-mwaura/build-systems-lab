# Deployment Guide

> This document walks through the full deployment workflow for `greeting-service` from Maven packaging, to Docker containerization, to full stack orchestration with Docker Compose, production logging, SonarQube analysis, and Snyk vulnerability scanning.
>
> The goal is not just to show commands. It explains what is happening at each stage, why each tool exists, and how all the pieces connect together. Someone following this guide from top to bottom should arrive at a fully running, observable, production-style application and understand exactly how it got there.

---

## The Story So Far

Before running a single command, it helps to understand the journey the application takes from source code to a running container.

The source code lives in a Maven multi-module project. Maven compiles it, runs tests, enforces coverage, and packages everything into a single executable JAR. Docker picks up that JAR, wraps it in a lightweight container image, and hands it to Docker Compose, which brings up the full stack the API, SonarQube, and Snyk as a set of connected, networked services.

```text
Source Code
    ↓
Maven Build → compile, test, coverage check, package
    ↓
Spring Boot Fat JAR
    ↓
Docker Multi-Stage Build → build image → runtime image
    ↓
Docker Compose Stack → greeting-api + sonarqube + snyk
    ↓
Running, Observable, Production-Style Application
```

Every step in that chain has a reason for existing, and this guide walks through each one.

---

## 1. Prerequisites

Before starting, make sure the following tools are installed and available on your machine.

| Tool | Purpose |
|---|---|
| Java 21 | Application runtime |
| Maven 3.9+ | Build system |
| Docker | Container engine |
| Docker Compose | Multi-container orchestration |
| Git | Source control |

Verify everything is in place:

```bash
java -version
mvn -version
docker --version
docker compose version
git --version
```

> 📸 **Screenshot:** [01-01-prerequisites-versions] Run all five commands above in your terminal.
> Show: version output for each tool confirming they are installed and available.

Clone the repository and move into the project root:

```bash
git clone https://github.com/Kate-mwaura/build-systems-lab.git
cd greeting-service
```

---

## 2. Project Structure

Before touching any commands, take a moment to understand how the project is laid out. This structure is intentional and everything that follows depends on it.

```bash
greeting-service/
│
├── pom.xml                    # Parent POM — the build controller
├── Dockerfile                 # Multi-stage Docker build
├── docker-compose.yml         # Full stack orchestration
│
└── greeting-api/              # Child module — the actual Spring Boot app
    ├── pom.xml                # Child POM — inherits from parent
    └── src/
        ├── main/
        │   ├── java/          # Application source code
        │   └── resources/     # Config files and logback
        └── test/              # Test classes
```

This is a Maven multi-module setup. The root project is the parent it manages dependency versions, plugin versions, profiles, and build configuration for everything beneath it. The `greeting-api` module is the child it contains the actual application and inherits all of that shared configuration without duplicating it.

This is a common pattern in production systems because it lets teams scale across multiple services without each service managing its own dependency versions independently. Version consistency is enforced in one place.

---

## 3. Building the Application

The first real step is the Maven build. From the project root:

```bash
mvn clean package
```

This single command triggers the full Maven lifecycle. Internally, Maven works through several phases in sequence:

| Phase | What Happens |
|---|---|
| `clean` | Deletes old build artifacts from `target/` |
| `compile` | Compiles `.java` source files into bytecode |
| `test` | Runs all tests using Surefire |
| `package` | Packages everything into an executable JAR |

Because this is a multi-module project, Maven processes the parent first, then the child. You will see this in the terminal output as the **Reactor Build Order** Maven's way of telling you which modules it will build and in what sequence.

```text
[INFO] Reactor Build Order:
[INFO] greeting-service    [pom]
[INFO] greeting-api        [jar]
```

The parent builds as a `pom` it has no source code of its own, just configuration. The child builds as a `jar` it produces the actual executable artifact.

After a successful build, the packaged artifact lives here:

```bash
greeting-api/target/greeting-api-1.0-SNAPSHOT.jar
```

This is a **fat JAR** a single self-contained file that includes the application classes, all dependencies, and an embedded Tomcat server. No external application server required. You can take this file anywhere Java 21 is installed and run it.

Verify the artifact was created:

```bash
ls greeting-api/target/
```

> 📸 **Screenshot:** [03-01-mvn-clean-package] Run `mvn clean package` from the project root.
> Show: Reactor Build Order, test execution, BUILD SUCCESS, total build time, and the reactor summary showing both modules succeeding.

> 📸 **Screenshot:** [03-02-target-artifact] Run `ls greeting-api/target/`.
> Show: the generated `greeting-api-1.0-SNAPSHOT.jar` file in the output.

---

## 4. Running Tests and Enforcing Coverage

Tests run automatically as part of `mvn clean package`, but it is worth running them explicitly to see the full output and understand what is being enforced.

```bash
mvn clean verify
```

`verify` runs everything `package` does, plus it executes the JaCoCo coverage check after the tests complete. This is the command that enforces the coverage gate.

### What JaCoCo Does

JaCoCo (Java Code Coverage) instruments the bytecode during the test phase and measures which instructions were actually executed by the tests. After the tests finish, it generates a coverage report and checks whether the result meets the configured minimum threshold.

This project enforces **80% instruction coverage**. If the tests do not cover at least 80% of the application's instructions, the build fails at the `verify` phase and the JAR never gets produced.

```text
[INFO] --- jacoco:0.8.11:check (check) @ greeting-api ---
[INFO] All coverage checks have been met.
```

If coverage drops below 80%, you will see:

```text
[ERROR] Rule violated for bundle greeting-api:
instructions covered ratio is 0.75, but expected minimum is 0.80
```

The JaCoCo HTML report is generated at:

```bash
greeting-api/target/site/jacoco/index.html
```

Open it in a browser to see a full breakdown of which classes, methods, and lines were covered.

> 📸 **Screenshot:** [04-01-jacoco-verify] Run `mvn clean verify` and show the JaCoCo check output.
> Show: `[INFO] All coverage checks have been met.` and the Tests run summary with 0 failures.

> 📸 **Screenshot:** [04-02-jacoco-report] Open `greeting-api/target/site/jacoco/index.html` in a browser.
> Show: the coverage report dashboard — overall instruction coverage percentage, class-level breakdown, and the highlighted covered/missed lines.

---

## 5. Running the Application Locally

Before containerizing, it is useful to run the application directly on the host to verify it works and to see how profiles behave.

Move into the child module:

```bash
cd greeting-api
```

Start with the default dev profile:

```bash
mvn spring-boot:run
```

Spring Boot starts up, activates the `dev` profile, and opens the application on port `9090`. Watch the startup logs they tell you exactly what is happening:

```text
The following 1 profile is active: "dev"
Tomcat initialized with port 9090 (http)
Started GreetingApplication in 3.2 seconds
```

Test the endpoint:

```bash
curl "http://localhost:9090/greet?name=Thony"
```

```text
Hello, Thony!
```

### What Spring Boot Does on Startup

When the application starts, Spring Boot works through a specific sequence internally:

1. Loads `application.properties` — reads the base configuration
2. Activates the current profile — loads `application-dev.properties` on top
3. Initializes logback — picks up `logback-spring.xml` and activates the dev logging block
4. Creates the embedded Tomcat server
5. Registers the REST controllers
6. Opens the configured port

This sequence is why profile-specific configuration works Spring Boot layers the profile file on top of the base file, so values in `application-dev.properties` override or extend whatever is in `application.properties`.

### Switching to the Production Profile

```bash
mvn spring-boot:run -Dspring-boot.run.profiles=prod
```

The application now runs on port `9080` with file-based logging instead of console output. Logs are written to:

```bash
greeting-api/logs/greeting-api.log
```

| Setting | Dev | Prod |
|---|---|---|
| Port | 9090 | 9080 |
| Log destination | Console | Rolling file |
| App log level | DEBUG | INFO |
| Framework log level | INFO | WARN |

> 📸 **Screenshot:** [05-01-spring-boot-run-dev] Run `mvn spring-boot:run` and show the startup output.
> Show: active profile (`dev`), Tomcat port 9090, started successfully, and the curl response `Hello, Kate!`.

> 📸 **Screenshot:** [05-02-spring-boot-run-prod] Run `mvn spring-boot:run -Dspring-boot.run.profiles=prod`.
> Show: active profile (`prod`), Tomcat port 9080, and then `cat logs/greeting-api.log` showing log entries written to file.

---

## 6. Understanding the Multi-Stage Docker Build

Running on the host works for development. Production systems deploy containers. This project uses a multi-stage Docker build, and understanding why it is structured in two stages matters before running the build command.

The Dockerfile lives at the project root not inside the child module. This is intentional. The build needs access to both the parent POM and the child module, and the Docker build context must include both.

### Stage 1 — Build Environment

```dockerfile
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
COPY greeting-api/pom.xml greeting-api/pom.xml
RUN mvn dependency:go-offline
COPY greeting-api/src greeting-api/src
RUN mvn clean package -pl greeting-api -am -DskipTests
```

This stage uses a full Maven + JDK image. Its only job is to compile the application and produce the JAR. It has everything needed to build Java code — Maven, the JDK, build tools, the works.

`dependency:go-offline` downloads all dependencies before copying the source code. This is a Docker caching optimization if the POM files haven't changed, Docker reuses the cached dependency layer and skips the download on subsequent builds.

### Stage 2 — Runtime Environment

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN mkdir -p /app/logs
COPY --from=build /build/greeting-api/target/*.jar app.jar
EXPOSE 9080
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
```

This stage starts fresh from a minimal JRE image. Maven is gone. The JDK is gone. Build tools are gone. Only the Java runtime and the packaged JAR remain.

The `COPY --from=build` instruction pulls just the final JAR from the build stage and drops it into the clean runtime image. This is the key advantage of multi-stage builds the final image is as small and clean as possible, with no build tooling that could become a security exposure in production.

`RUN mkdir -p /app/logs` creates the logs directory before the application starts, so Logback finds it ready and can write immediately.

---

## 7. Building the Docker Image

From the project root not from inside `greeting-api`:

```bash
docker build -t greeting-api:latest .
```

The `.` at the end is important. It sets the build context to the current directory, which means Docker can see both the parent `pom.xml` and the `greeting-api/` folder. Running this from inside `greeting-api/` would break the build because the parent POM would not be in scope this was a real issue encountered during the project setup.

Watch the output as Docker works through each stage. You will see Maven downloading dependencies, compiling the source, running the build, and then the second stage pulling the lightweight runtime image and copying the JAR in.

Verify the image was created:

```bash
docker images
```

> 📸 **Screenshot:** [07-01-docker-build] Run `docker build -t greeting-api:latest .` from the project root.
> Show: both build stages executing, Maven packaging output, BUILD SUCCESS inside the container, and the final image creation line.

> 📸 **Screenshot:** [07-02-docker-images] Run `docker images`.
> Show: `greeting-api:latest` in the list with its size — notice how much smaller it is compared to the Maven build image.

---

## 8. Running the Docker Container

With the image built, run the container:

```bash
docker run -p 9080:9080 greeting-api:latest
```

The `-p 9080:9080` flag maps port 9080 on your host machine to port 9080 inside the container. The format is always `HOST_PORT:CONTAINER_PORT`. Without this mapping, the application runs inside the container but nothing on your machine can reach it.

The prod profile activates automatically the `ENTRYPOINT` in the Dockerfile passes `-Dspring.profiles.active=prod` to the JVM at startup.

Test the endpoint:

```bash
curl "http://localhost:9080/greet?name=Kate"
```

```text
Hello, Kate!
```

### Inspecting the Running Container

Open a second terminal and find the container ID:

```bash
docker ps
```

Drop into the container filesystem:

```bash
docker exec -it <container-id> sh
```

Look at what is inside:

```bash
ls -la /app
```

```text
/app
├── logs/
│   └── greeting-api.log
└── app.jar
```

> 📸 **Screenshot:** [08-01-docker-run] Run `docker run -p 9080:9080 greeting-api:latest`.
> Show: Spring Boot startup banner, active profile (`prod`), Tomcat started on port 9080.

> 📸 **Screenshot:** [08-02-curl-response] Run `curl "http://localhost:9080/greet?name=Kate"`.
> Show: `Hello, Kate!` response.

> 📸 **Screenshot:** [08-03-docker-exec] Run `docker ps` then `docker exec -it <container-id> sh` and `ls -la /app`.
> Show: the container running, the `/app` directory with `app.jar` and the `logs/` folder.

---

## 9. Production Logging Inside the Container

The production logging setup is one of the more interesting parts of this project because it behaves differently from anything you see locally in development.

In dev, logs stream directly to the console you see everything in real time. In prod, logs go to a rolling file appender inside the container at `/app/logs/greeting-api.log`. Nothing appears on the console. This is intentional production systems ship logs to centralized platforms, not stdout.

Send a few requests to generate log entries:

```bash
curl "http://localhost:9080/greet?name=Kate"
curl "http://localhost:9080/greet?name=World"
curl "http://localhost:9080/greet?name=Thony"
```

Now read the log file from inside the container:

```bash
docker exec <container-id> cat app/logs/greeting-api.log
```

You should see timestamped entries for each request:

```text
2026-05-12 07:14:32 [http-nio-9080-exec-1] INFO  c.e.g.controller.GreetingController - Processing greeting request for: Kate
2026-05-12 07:14:44 [http-nio-9080-exec-2] INFO  c.e.g.controller.GreetingController - Processing greeting request for: World
2026-05-12 07:18:21 [http-nio-9080-exec-3] INFO  c.e.g.controller.GreetingController - Processing greeting request for: Thony
```

### How the Rolling Policy Works

The log file is configured to rotate based on two conditions — whichever comes first:

- the file reaches **10MB**
- a new time window opens based on the archive filename pattern

Archived logs are compressed and stored at:

```bash
/app/logs/archive/application-{date}.{index}.log.gz
```

A 30-day retention policy and a 1GB total size cap prevent the container disk from filling up over time. In a real deployment, the `/app/logs/` directory would be mounted as an external Docker volume so logs survive container restarts and can be consumed by a log shipper like Fluentd or Promtail.

> 📸 **Screenshot:** [09-01-container-logs] Run the curl commands then `docker exec <container-id> cat app/logs/greeting-api.log`.
> Show: the log file with timestamped request entries, thread names, log levels, and class names visible.

---

## 10. Docker Compose — Running the Full Stack

Running a single container works for isolated testing. The real environment this project is designed for is the full Docker Compose stack the API, SonarQube, and Snyk running together as a networked set of services.

From the project root:

```bash
docker compose up --build
```

`--build` forces Docker Compose to rebuild the `greeting-api` image before starting. If you have made code changes since the last build, always include this flag.

Docker Compose now:

1. Builds the `greeting-api` image
2. Pulls `sonarqube:lts-community` if not already present
3. Pulls `snyk/snyk:maven` if not already present
4. Creates an isolated bridge network (`greetings-net`)
5. Starts all three containers connected to that network
6. Streams logs from all services in the terminal

### Internal Networking

Inside the Compose network, containers communicate with each other by **service name** instead of IP address or localhost. This means `greeting-api` can reach SonarQube at `http://sonarqube:9000` without any manual network configuration. Docker automatically creates DNS entries for each service name.

This is a fundamental production concept. In a real Kubernetes environment, the same principle applies — services discover each other by name, not by IP.

### Services After Startup

| Service | URL | Notes |
|---|---|---|
| greeting-api | http://localhost:9080 | Prod profile active |
| SonarQube | http://localhost:9000 | Wait ~60s for full startup |

### Stopping the Stack

```bash
docker compose down
```

This removes the containers and the network. Volumes persist unless you add `-v`.

> 📸 **Screenshot:** [10-01-compose-up] Run `docker compose up --build`.
> Show: image build stage, all three containers starting, the Compose network being created, and log output streaming from multiple services.

> 📸 **Screenshot:** [10-02-compose-ps] Run `docker ps` after Compose is up.
> Show: all three containers running — `greeting-api`, `sonarqube`, and `snyk`.

> 📸 **Screenshot:** [10-03-sonarqube-up] Open `http://localhost:9000` in a browser.
> Show: the SonarQube login page confirming it is running.

---

## 11. SonarQube Code Quality Analysis

With the full stack running, you can now run a static analysis of the codebase against the SonarQube instance.

SonarQube scans the project for:

- code smells
- bugs
- duplicated code
- maintainability issues
- test coverage metrics
- technical debt estimates

Make sure SonarQube is fully started it takes about 60 seconds. Then from the `greeting-api` directory:

```bash
mvn clean verify sonar:sonar \
  -Dsonar.host.url=http://localhost:9000 \
  -Dsonar.login=${SONAR_TOKEN}
```

`clean verify` runs the full build and test cycle including JaCoCo coverage generation. `sonar:sonar` then ships the compiled bytecode, source files, and coverage data to SonarQube for analysis.

### What SonarQube Does Internally

1. Reads the compiled bytecode from `target/classes`
2. Reads the JaCoCo coverage report from `target/site/jacoco`
3. Analyzes source structure, complexity, and duplication
4. Calculates maintainability scores and technical debt
5. Checks results against any configured quality gate
6. Publishes the full dashboard to `http://localhost:9000`

### Reading the Dashboard

Once the analysis completes, open `http://localhost:9000` and navigate to the `greeting-api` project.

The dashboard shows:

- **Reliability** — bugs found in the code
- **Security** — vulnerabilities and hotspots
- **Maintainability** — code smells and technical debt
- **Coverage** — percentage of code covered by tests (sourced from JaCoCo)
- **Duplications** — repeated code blocks

A green quality gate means the project meets the defined thresholds. A red gate means something needs to be fixed before the code would be allowed to ship in a real CI/CD pipeline.

> 📸 **Screenshot:** [11-01-sonar-analysis] Run the `mvn clean verify sonar:sonar` command.
> Show: Maven lifecycle running, JaCoCo report generation, and the final SonarQube analysis completion line with the dashboard URL.

> 📸 **Screenshot:** [11-02-sonar-dashboard] Open `http://localhost:9000` and navigate to the project dashboard.
> Show: the full SonarQube dashboard — quality gate status, coverage percentage, code smells count, reliability and security ratings.

> 📸 **Screenshot:** [11-03-sonar-coverage] Click into the Coverage section.
> Show: the detailed coverage breakdown linked from the JaCoCo report.

---

## 12. Snyk Dependency Vulnerability Scanning

Snyk is included in the Compose stack as a security scanner. It checks the project's Maven dependencies and the container image for known CVEs and vulnerable transitive dependencies.

This adds a security layer to the deployment workflow that mirrors what engineering teams run in real CI/CD pipelines.

### Scanning Project Dependencies

```bash
docker compose run snyk snyk test --all-projects
```

Snyk reads the `pom.xml`, resolves the full dependency tree, and checks every library including transitive dependencies against its vulnerability database.

### Scanning the Container Image

```bash
docker compose run snyk snyk container test greeting-api:latest
```

This scans the actual Docker image for OS-level and runtime vulnerabilities, not just the Java dependencies.

### What the Output Means

Snyk reports vulnerabilities by severity **Critical**, **High**, **Medium**, and **Low**. For each vulnerability it shows:

- which package introduced it
- the CVE identifier
- whether a fix is available (and which version to upgrade to)
- whether it comes from a direct or transitive dependency

In a CI/CD pipeline, Snyk can be configured to fail the build if any High or Critical vulnerabilities are found, blocking deployment until they are resolved.

> ⚠️ **Important:** Your `SNYK_TOKEN` must be set as an environment variable. Store it in a `.env` file at the project root and add `.env` to `.gitignore`. Never commit tokens to version control.
>
> ```bash
> # .env
> SNYK_TOKEN=your_token_here
> ```

> 📸 **Screenshot:** [12-01-snyk-test] Run `docker compose run snyk snyk test --all-projects`.
> Show: the dependency tree being scanned, vulnerability summary output, and severity breakdown.

> 📸 **Screenshot:** [12-02-snyk-container] Run `docker compose run snyk snyk container test greeting-api:latest`.
> Show: the image scan output with any flagged vulnerabilities or a clean result.

---

## 13. Real Troubleshooting Encountered

The path to a working deployment was not linear. These are the real issues encountered during the project and how they were resolved.

---

### Parent POM Not Found During Docker Build

**Problem:**

```text
Non-resolvable parent POM for com.example:greeting-api
```

**Cause:** The Docker build was being run from inside the `greeting-api/` directory. The build context did not include the parent `pom.xml`, so Maven could not resolve the inheritance chain.

**Fix:** Always run the Docker build from the project root where both the parent POM and the child module are visible to the build context.

```bash
# Wrong — run from greeting-api/
docker build -t greeting-api .

# Correct — run from greetings-service/
docker build -t greeting-api:latest .
```

---

### Fat JAR Not Building — `no main manifest attribute`

**Problem:**

```text
no main manifest attribute, in greeting-api-1.0-SNAPSHOT.jar
```

**Cause:** The `spring-boot-maven-plugin` was declared in the parent `pluginManagement` without a version, and the `repackage` goal was not explicitly wired in the child POM. Maven built a thin JAR instead of an executable fat JAR.

**Fix:** Add the version to the plugin in the parent `pluginManagement`, and add the `repackage` execution explicitly in the child POM.

```xml
<execution>
    <goals>
        <goal>repackage</goal>
    </goals>
</execution>
```

---

### `mvn spring-boot:run` Failing — "Unable to find a suitable main class"

**Problem:**

```text
Unable to find a suitable main class, please add a 'mainClass' property
```

**Cause:** The command was run from the project root against the parent POM, which has `<packaging>pom</packaging>` and no application code.

**Fix:** Run from inside the child module, or target it explicitly.

```bash
cd greeting-api
mvn spring-boot:run

# Or from root:
mvn spring-boot:run -pl greeting-api
```

---

### Logback `No appenders present` — Application Crashing in Docker

**Problem:**

```text
Appender named [CONSOLE] not referenced. Skipping further processing.
No appenders present in context [default]
```

**Cause:** The `<springProfile>` blocks in `logback-spring.xml` were not activating because no Spring profile was set at the point logback initialized. The appenders were defined but never attached to any logger.

**Fix:** Add a fallback `<springProfile>` block for when no profile is active, and ensure the `logs/` directory exists inside the container before the app starts.

```xml
<springProfile name="!dev &amp; !prod">
    <root level="INFO">
        <appender-ref ref="CONSOLE" />
    </root>
</springProfile>
```

```dockerfile
RUN mkdir -p /app/logs
```

---

### Port Conflicts

**Problem:**

```text
Web server failed to start. Port 8080 was already in use.
```

**Cause:** The base `application.properties` did not have a profile active, so Spring fell back to the `default` profile and used port 8080, which was occupied by another process.

**Fix:** Set the default active profile explicitly in `application.properties`.

```properties
spring.profiles.active=dev
```

---

### `spring.profiles.active` Invalid in Profile-Specific Files

**Problem:**

```text
Property 'spring.profiles.active' imported from location 'application-dev.properties' is invalid
```

**Cause:** Spring Boot does not allow `spring.profiles.active` to be declared inside profile-specific files like `application-dev.properties`. It only belongs in the base `application.properties`.

**Fix:** Remove `spring.profiles.active` from all profile-specific files. It lives only in `application.properties`.

---

## 14. Production Considerations

This project runs as a local deployment lab, but the architecture is intentionally designed to move toward real production environments with minimal changes.

| Area | Current State | Production Upgrade |
|---|---|---|
| Reverse Proxy | Direct port exposure | NGINX or Traefik |
| HTTPS | Not configured | TLS termination at proxy |
| Container Orchestration | Docker Compose | Kubernetes |
| Monitoring | Not configured | Prometheus + Grafana |
| Log Aggregation | File-based | ELK Stack or Grafana Loki |
| CI/CD | Manual commands | GitHub Actions or Jenkins |
| Secrets Management | `.env` file | Vault or Kubernetes Secrets |
| Image Registry | Local | Docker Hub or Harbor |

The decisions made throughout this project multi-stage builds, profile-driven config, externalized secrets, rolling log policies, quality gates are all choices that translate directly into production-grade systems. The tooling scales; the patterns stay the same.

---

## 15. Final Thoughts

This project started as a simple Spring Boot greeting service and grew into something that genuinely reflects how backend systems are built and shipped in real engineering environments.

The goal was never just to have a working REST endpoint. The real objective was understanding the full picture how source code becomes a packaged artifact, how that artifact becomes a container, how containers are orchestrated as a networked stack, how logging behaves differently across environments, how quality is enforced before code ships, and how security is scanned as part of the build process.

Every tool in this stack exists for a reason. Maven manages the build. Docker isolates the runtime. Compose orchestrates the services. Logback makes the application observable. SonarQube enforces quality. Snyk enforces security. None of them are there for show.

The troubleshooting section of this guide is as valuable as the deployment steps themselves. Real engineering is not following a clean path from A to B it is knowing what breaks, understanding why it broke, and knowing how to fix it without guessing.

That is what this project is really demonstrating.