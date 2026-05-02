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

This directory is temporary and should not be committed to version control.

---

