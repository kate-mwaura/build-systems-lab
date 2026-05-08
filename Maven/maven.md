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

---

## Plugins and Plugin Management (Execution Layer)

### What are plugins?

Plugins are the execution layer of Maven. They are responsible for performing actual build tasks such as compiling source code, running tests, packaging artifacts, and generating reports. Instead of Maven doing the work itself, it delegates these operations to plugins.

For example:
- maven-compiler-plugin → compiles Java code into bytecode  
- maven-surefire-plugin → runs unit tests before packaging  

### Plugin structure

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.jacoco</groupId>
            <artifactId>jacoco-maven-plugin</artifactId>
            <version>0.8.11</version>

            <executions>
                <execution>
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

                <execution>
                    <id>check</id>
                    <goals>
                        <goal>check</goal>
                    </goals>
                    <configuration>
                        <rules>
                            <rule>
                                <element>BUNDLE</element>
                                <limits>
                                    <limit>
                                        <counter>INSTRUCTION</counter>
                                        <value>COVEREDRATIO</value>
                                        <minimum>0.90</minimum>
                                    </limit>
                                </limits>
                            </rule>
                        </rules>
                    </configuration>
                </execution>
            </executions>

        </plugin>
    </plugins>
</build>
```

### Plugin tag breakdown

#### build

Defines how the project is built. This is where plugins are configured and executed.

```xml
<build>...</build>
```

#### plugins

Container that holds all plugins used in the project.

```xml
<plugins>...</plugins>
```

#### plugin

Defines a single plugin used during the build process.

```xml
<plugin>
    <groupId>org.example</groupId>
    <artifactId>example-plugin</artifactId>
    <version>1.0.0</version>
</plugin>
```

#### GAV (Plugin Identity)

Identifies the plugin uniquely in Maven repositories.

```xml
<groupId>org.example</groupId>
<artifactId>example-plugin</artifactId>
<version>1.0.0</version>
```

#### executions

Defines when and how a plugin runs in the build lifecycle.

```xml
<executions>...</executions>
```

#### execution

Represents a single run of the plugin with its own configuration.

```xml
<execution>
    <id>example</id>
</execution>
```

#### id

Unique identifier for a specific execution or configuration.

```xml
<id>report</id>
```

#### configuration

Defines custom parameters that control plugin behavior.

```xml
<configuration>...</configuration>
```

### Plugins phases and goals

Phases define when an action happens in the Maven lifecycle.  
Goals define what action the plugin performs during that phase.

### Common plugins

| plugin | phase | goal |
|--------|-------|------|
| maven-clean-plugin | clean | clean:clean - deletes the target directory |
| maven-compiler-plugin | compile | compiler:compile - compiles main source code |
| maven-compiler-plugin | test-compile | compiler:testCompile - compiles test code |
| maven-surefire-plugin | test | surefire:test - runs unit tests |
| maven-jar-plugin | package | jar:jar - packages code into a jar |
| spring-boot-maven-plugin | package | spring-boot:repackage - builds executable fat jar |
| maven-install-plugin | install | install:install - installs artifact to local repo |
| maven-deploy-plugin | deploy | deploy:deploy - pushes artifact to remote repo |
| maven-resources-plugin | process-resources | resources:resources - copies resource files |
| jacoco-maven-plugin | test | jacoco:report - generates coverage report |
| jacoco-maven-plugin | verify | jacoco:check - enforces coverage rules |

### Plugin management

Plugin management defines plugin versions centrally without executing them.  
It is mainly used in parent projects so child modules inherit consistent plugin versions.

### Plugin management structure

```xml
<build>
    <pluginManagement>
        <plugins>
            <plugin>
                <groupId>org.example</groupId>
                <artifactId>example-plugin</artifactId>
                <version>1.0.0</version>
            </plugin>
        </plugins>
    </pluginManagement>
</build>
```

---

## Distribution Management

### What distribution management is

`distributionManagement` defines where Maven should publish the final built artifacts after a successful build.

This is the deployment layer of Maven. Instead of keeping the generated `.jar` or `.war` file locally inside the `target/` directory, Maven pushes the artifact to remote repositories where other developers, services, or deployment pipelines can access it.

In production environments this is commonly used with:
- Nexus
- Artifactory
- GitHub Packages
- Internal company repositories

Maven uses the repository `id` defined in `distributionManagement` to communicate with matching credentials stored inside the `settings.xml` file.

### Distribution management structure

```xml
<distributionManagement>

    <repository>
        <id>company-releases</id>
        <name>Company Release Repository</name>
        <url>https://repo.example.com/repository/maven-releases/</url>
    </repository>

    <snapshotRepository>
        <id>company-snapshots</id>
        <name>Company Snapshot Repository</name>
        <url>https://repo.example.com/repository/maven-snapshots/</url>
    </snapshotRepository>

</distributionManagement>
```

### Distribution management tag breakdown

#### distributionManagement

Defines where Maven should publish built artifacts after packaging.

```xml
<distributionManagement>...</distributionManagement>
```

#### repository

Defines the repository where stable production-ready releases are deployed.

```xml
<repository>
    <id>company-releases</id>
    <url>https://repo.example.com/repository/maven-releases/</url>
</repository>
```

#### snapshotRepository

Defines where development or unstable snapshot versions are deployed.

Snapshot versions are usually under active development.

```xml
<snapshotRepository>
    <id>company-snapshots</id>
    <url>https://repo.example.com/repository/maven-snapshots/</url>
</snapshotRepository>
```

#### id

Unique identifier used to link the repository with credentials inside the `settings.xml` file.

The IDs inside `distributionManagement` and `settings.xml` must match for authentication to work.

```xml
<id>company-releases</id>
```

#### url

Defines the repository endpoint Maven communicates with when uploading artifacts.

```xml
<url>https://repo.example.com/repository/maven-releases/</url>
```

---

## settings.xml

### What settings.xml is

The `settings.xml` file is Maven’s local configuration file located inside the `.m2` directory.

```bash
~/.m2/settings.xml
```

Unlike the `pom.xml`, the `settings.xml` file is machine-specific and is mainly used to store:
- repository credentials
- mirrors
- private repository configurations
- proxy settings
- active profiles

This file should never be committed to version control because it can contain sensitive credentials like usernames, passwords, and access tokens.

The `pom.xml` defines *what* repository Maven should communicate with, while the `settings.xml` file defines *how* Maven authenticates and connects to those repositories.

### settings.xml structure

```xml
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
          https://maven.apache.org/xsd/settings-1.0.0.xsd">

    <mirrors>
        <mirror>
            <id>company-mirror</id>
            <name>Internal Maven Mirror</name>
            <url>https://repo.example.com/repository/maven-public/</url>
            <mirrorOf>*</mirrorOf>
        </mirror>
    </mirrors>

    <servers>
        <server>
            <id>company-releases</id>
            <username>${env.MAVEN_REPO_USER}</username>
            <password>${env.MAVEN_REPO_PASSWORD}</password>
        </server>
        <server>
            <id>company-snapshots</id>
            <username>${env.MAVEN_REPO_USER}</username>
            <password>${env.MAVEN_REPO_PASSWORD}</password>
        </server>
    </servers>

    <profiles>
        <profile>
            <id>internal-repositories</id>

            <repositories>
                <repository>
                    <id>company-public</id>
                    <url>https://repo.example.com/repository/maven-public/</url>
                </repository>
            </repositories>
        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>internal-repositories</activeProfile>
    </activeProfiles>

</settings>
```

### settings.xml tag breakdown

#### mirrors

Defines alternative repositories Maven should use when downloading dependencies.

Mirrors act like routing rules and can force Maven to fetch dependencies from a specific repository instead of Maven Central.

```xml
<mirrors>...</mirrors>
```

#### mirror

Defines a single mirror repository Maven should communicate with.

```xml
<mirror>
    <id>company-mirror</id>
    <url>https://repo.example.com/repository/maven-public/</url>
    <mirrorOf>*</mirrorOf>
</mirror>
```

#### mirrorOf

Specifies which repositories should be redirected to the mirror.

`*` means all repositories.

```xml
<mirrorOf>*</mirrorOf>
```

#### servers

Container for authentication credentials used when Maven uploads or downloads artifacts.

```xml
<servers>...</servers>
```

#### server

Defines login credentials for a specific repository.

The `id` must match the repository ID inside `distributionManagement`.

```xml
<server>
    <id>company-releases</id>
    <username>${env.MAVEN_REPO_USER}</username>
    <password>${env.MAVEN_REPO_PASSWORD}</password>
</server>
```

#### username and password

Credentials Maven uses to authenticate against private repositories.

Production environments usually inject these values through environment variables instead of hardcoding secrets.

```xml
<username>${env.MAVEN_REPO_USER}</username>
<password>${env.MAVEN_REPO_PASSWORD}</password>
```

