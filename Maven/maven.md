# 🔹 Introduction

## Why Maven matters in DevOps

Maven is a build automation and dependency management tool for Java applications. In a DevOps workflow, it is responsible for turning source code into reproducible, deployable artifacts. It handles dependency resolution, compiles `.java` source into JVM bytecode, executes test suites, and packages the application into a versioned artifact (JAR/WAR).

Maven is typically integrated into CI/CD pipelines to enforce consistent builds, automate validation steps, and ensure artifacts are production-ready.

---

## What I aimed to learn

My goal in learning Maven was to understand the full build lifecycle — from raw `.java` source code to a packaged artifact ready for deployment. I focused on how Maven manages dependencies, executes builds through defined lifecycles, enforces code quality, and integrates testing and security tools to produce reliable, production-grade builds.

---

## What this document covers

This document captures my hands-on experience working with Maven in a DevOps context. It covers core concepts such as project structure, dependency management, build lifecycles, plugins, and integrations with tools like SonarQube, JaCoCo, Snyk, and Docker.

The goal is to provide a practical reference that reflects both conceptual understanding and real-world implementation.

---

# 🔹 What is Maven

## Definition

Maven is an Apache open-source build automation and dependency management tool used primarily for Java applications. It provides a standardized way to define how a project is built, tested, and packaged into a deployable artifact.

---

## Why Maven exists (Problem it solves)

Before tools like Maven, building Java applications was inconsistent and manual. Developers had to manage dependencies, compilation, testing, and packaging individually, which often led to broken builds and environment-specific issues.

Maven solves this by:
- providing a consistent way to build projects  
- handling dependencies automatically  
- enforcing a standard project structure   
- enabling reproducible builds across environments [ensuring builds work the same across different environments]

---

## Where Maven fits in a DevOps workflow

In a DevOps pipeline, Maven is responsible for the build and validation stage. It is used to:

- resolve and manage project dependencies  
- compile source code into JVM bytecode  
- execute automated tests  
- integrate code quality and security tools (e.g., SonarQube, Snyk)  
- package the application into a versioned artifact (JAR/WAR)  

This artifact is then used in later stages such as containerization (Docker) and deployment.

---

# 🔹 Maven Project Structure

## Standard Directory Layout

```bash
project-root/
│
├── pom.xml
│
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com/example/project/
│   │   │       ├── Application.java
│   │   │       └── controller/
│   │   │           └── GreetingController.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── logback.xml
│   │
│   └── test/
│       ├── java/
│       │   └── com/example/project/
│       │       └── ApplicationTests.java
│       │
│       └── resources/
│
└── target/
    ├── classes/
    ├── test-classes/
    └── project-name.jar

```
---
## Key Files and Directories

### `pom.xml`

The `pom.xml` (Project Object Model) is the core configuration file of a Maven project.

It defines:
- project dependencies  
- build configuration  
- plugins  
- project metadata (name, version, packaging)  

It acts as the **source of truth** for how the project is built, tested, and packaged.

---

### `src/main/java`

This directory contains the main application source code.

All production-ready `.java` files are placed here, following the package structure (e.g., `com.example.project`).

---

### `src/test/java`

This directory contains test classes.

These are used to validate application behavior using testing frameworks like JUnit. Maven automatically runs these tests during the build process.

---

### `src/main/resources`

This directory stores configuration and resource files used by the application.

Common files include:
- `application.properties` → application configuration  
- `logback.xml` → logging configuration  
- static files (HTML, CSS, JSON)

These files are included in the final build artifact.

---

### `target/`

The `target` directory is generated automatically when a build is executed.

It contains:
- compiled `.class` files  
- test results and reports  
- the final packaged artifact (JAR/WAR)  

This directory is temporary and should not be committed to version control should be added to the .gitignore file.

---

## Why Convention Matters

Maven follows a convention over configuration approach.

This means:
- you don’t need to define where your source code or tests are  
- Maven already expects files in specific locations  
- plugins and dependencies rely on this structure to work correctly  

This reduces the need for manual configuration and makes projects easier to:
- understand  
- maintain  
- run across different environments  

---

## What Happens When Convention is Broken

If the standard structure is not followed, Maven and its plugins may not work as expected.

Common issues include:

- tests are not executed if they are not located in `src/test/java`  
- plugins fail to run because they cannot find the expected files  
- compilation issues if source files are misplaced  
- incomplete or broken build artifacts due to missing classes  

Following Maven’s structure ensures that the build process runs smoothly without additional configuration.

---

## 🔹pom.xml (Heart of Maven)

The `pom.xml` (Project Object Model) is the central configuration file in a Maven project. It defines everything required to build, test, and package an application.

It acts as the **single source of truth**, controlling:

- project identity  
- dependencies  
- plugins  
- build behavior  
- artifact distribution  

In a DevOps workflow, this file directly connects:

`code → build → test → artifact → deployment`

---

```xml
<!-- Root element: defines this as a Maven project -->
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">

    <!-- Maven model version -->
    <modelVersion>4.0.0</modelVersion>

    <!-- ===================== -->
    <!-- Project Identity (GAV) -->
    <!-- ===================== -->
    <!-- Defines unique identity of the project -->
    <groupId>com.example</groupId>
    <artifactId>greeting-service</artifactId>
    <version>1.0.0</version>

    <!-- Packaging type: jar, war, etc -->
    <packaging>jar</packaging>

    <!-- ===================== -->
    <!-- Project Metadata -->
    <!-- ===================== -->
    <name>greeting-service</name>
    <description>Sample Spring Boot service</description>

    <!-- ===================== -->
    <!-- Parent (Inheritance) -->
    <!-- ===================== -->
    <!-- Used to inherit configurations (common in Spring Boot projects) -->
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.0.5</version>
    </parent>

    <!-- ===================== -->
    <!-- Properties (Centralized Values) -->
    <!-- ===================== -->
    <!-- Used to define reusable variables -->
    <properties>
        <java.version>21</java.version>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
    </properties>

    <!-- ===================== -->
    <!-- Dependency Management (Version Control Layer) -->
    <!-- ===================== -->
    <!-- Defines versions but does NOT import dependencies -->
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.example</groupId>
                <artifactId>example-bom</artifactId>
                <version>1.0.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <!-- ===================== -->
    <!-- Dependencies (Actual Libraries Used) -->
    <!-- ===================== -->
    <dependencies>

        <!-- Example dependency -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>

        <!-- Test dependency -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Example with exclusion -->
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>example-lib</artifactId>
            <version>1.2.3</version>
            <exclusions>
                <exclusion>
                    <groupId>org.unwanted</groupId>
                    <artifactId>bad-lib</artifactId>
                </exclusion>
            </exclusions>
        </dependency>

    </dependencies>

    <!-- ===================== -->
    <!-- Build Configuration -->
    <!-- ===================== -->
    <build>

        <!-- Plugin Management (Locks plugin versions) -->
        <pluginManagement>
            <plugins>
                <plugin>
                    <groupId>org.example</groupId>
                    <artifactId>example-plugin</artifactId>
                    <version>1.0.0</version>
                </plugin>
            </plugins>
        </pluginManagement>

        <!-- Plugins (Executed during build lifecycle) -->
        <plugins>

            <!-- Example plugin -->
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>

                <!-- Plugin configuration -->
                <configuration>
                    <source>21</source>
                    <target>21</target>
                </configuration>
            </plugin>

            <!-- Plugin with execution -->
            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>

                <executions>
                    <execution>
                        <id>prepare-agent</id>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>

                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>

        </plugins>
    </build>

    <!-- ===================== -->
    <!-- Distribution Management -->
    <!-- ===================== -->
    <!-- Defines where artifacts are published -->
    <distributionManagement>
        <repository>
            <id>release-repo</id>
            <url>https://repo.example.com/releases</url>
        </repository>

        <snapshotRepository>
            <id>snapshot-repo</id>
            <url>https://repo.example.com/snapshots</url>
        </snapshotRepository>
    </distributionManagement>

</project>
```

---

### Project (Root Element)

`<project>` is the root container of the POM. It defines this file as a Maven project and includes the XML schema used to validate its structure.

```xml
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
```

---

### modelVersion

Defines the version of the POM model Maven should use. This ensures Maven parses the file correctly.

```xml
<modelVersion>4.0.0</modelVersion>
```

---

### GAV (Project Identity)

Defines the unique identity of the project in Maven repositories.

- `groupId` → namespace (organization/domain)  
- `artifactId` → project name  
- `version` → release version  

```xml
<groupId>com.example</groupId>
<artifactId>greeting-service</artifactId>
<version>1.0.0</version>
```

---

### packaging

Specifies the type of artifact Maven should produce after build.

Common types:
- `jar` → standard Java application  
- `war` → web application  

```xml
<packaging>jar</packaging>
```

---

### name & description

Provides human-readable metadata for the project. Useful in repositories and documentation.

```xml
<name>greeting-service</name>
<description>Sample Spring Boot service</description>
```

---

### parent

Defines a parent POM to inherit configuration from (commonly Spring Boot).

This allows reuse of:
- dependency versions  
- plugin configurations  
- default build settings  

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>4.0.5</version>
</parent>
```

---

### properties

Defines reusable variables across the POM. This centralizes configuration and avoids duplication.

Typical use cases:
- Java version  
- encoding  
- dependency versions  

```xml
<properties>
    <java.version>21</java.version>
    <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
</properties>
```

---

## 🔹 Dependencies and Dependency Management

### What are Dependencies?

Dependencies are pre-built libraries that an application requires to run. Instead of writing everything from scratch, developers reuse existing, tested code from external sources like Maven Central.

Example: Instead of building a logging system, you import `logback` or `slf4j`.

---

### Transitive Dependencies

Transitive dependencies are dependencies required by another dependency.

Maven automatically:
- downloads your declared dependency  
- resolves and downloads all its required dependencies  

This keeps the `pom.xml` clean and avoids manually managing large dependency trees.

---

### Direct vs Transitive Dependencies

- **Direct dependencies** → explicitly defined in `pom.xml`  
- **Transitive dependencies** → pulled in automatically by Maven  

---

### How Maven Resolves Dependencies

During the build:

1. Maven first checks the dependencies in the **local repository** (`~/.m2/repository`)  
2. If not found, it downloads from **remote repositories (Maven Central)**  
3. Dependencies are cached locally in **~/.m2/repository**  for future builds  

---

## Dependencies Structure

```xml
<dependencies>

    <dependency>
        <groupId>org.example</groupId>
        <artifactId>example-lib</artifactId>
        <version>1.2.3</version>

        <scope>test</scope>

        <exclusions>
            <exclusion>
                <groupId>org.unwanted</groupId>
                <artifactId>bad-lib</artifactId>
            </exclusion>
        </exclusions>
    </dependency>

</dependencies>
```

---

## Breaking Down Dependency Tags

### GAV (Dependency Identity)

Identifies a dependency uniquely:

```xml
<groupId>org.example</groupId>
<artifactId>example-lib</artifactId>
<version>1.2.3</version>
```

---

### scope

Controls where and how a dependency is used.

Common scopes:

- **compile** (default) → available everywhere  
- **test** → only used during testing  
- **provided** → provided by runtime (e.g. servlet container)  
- **runtime** → not needed at compile time, required at runtime    
- **import** → used with BOM in dependencyManagement  

```xml
<scope>test</scope>
```

---

### exclusions

Used to remove unwanted transitive dependencies.

Helps:
- avoid conflicts  
- reduce vulnerabilities  
- keep builds clean  

```xml
<exclusions>
    <exclusion>
        <groupId>org.unwanted</groupId>
        <artifactId>bad-lib</artifactId>
    </exclusion>
</exclusions>
```

---

## Dependency Management

Defines dependency versions without actually importing them.

Used mainly in:
- parent projects  
- multi-module architectures  

Child modules inherit versions from here.

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.example</groupId>
            <artifactId>example-bom</artifactId>
            <version>1.0.0</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

---

## BOM (Bill of Materials)

A BOM is a special POM that defines **versions for multiple dependencies** without including them.

Purpose:
- avoid version conflicts  
- centralize version control  
- simplify dependency declarations  

### Why Spring Boot Parent Acts Like a BOM

Spring Boot parent:
- manages dependency versions  
- manages plugin versions  

This allows you to define only:
```xml
<groupId>org.springframework.boot</groupId>
<artifactId>spring-boot-starter-web</artifactId>
```

Without specifying a version, it is inherited.

---

## Dependency Conflict Resolution

### How Maven Resolves Conflicts

Maven uses **"nearest definition wins"**:

- The dependency closest to your project in the tree is selected  
- Other versions are ignored  

---

### Best Practices to Avoid Conflicts

- Use **dependencyManagement** to enforce versions  
- Use **BOMs** (e.g. Spring Boot)  
- Avoid mixing multiple versions of the same library  
- Use **exclusions** to remove conflicting dependencies  
- Regularly scan dependencies (Snyk, OWASP, etc.)  

---

### DevOps Insight

Dependency management directly impacts:

- build stability  
- security (vulnerabilities)  
- reproducibility across environments  

Poor dependency control = unstable builds + production risks