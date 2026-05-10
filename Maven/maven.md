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
```
<details>
  <summary>Click to expand: Full pom.xml</summary>

```xml
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

</details>

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

---

### Maven Profiles
---
Profiles are environment-specific configurations Maven uses to customize how a project builds and behaves under different environments.

They allow a single project to have different:
- dependencies
- properties
- plugins
- repositories
- build configurations

without modifying the main `pom.xml`.

Profiles are commonly used for:
- development environments
- testing environments
- staging deployments
- production deployments
- internal company repositories

Maven profiles can be defined inside:
- `pom.xml`
- `settings.xml`

The `pom.xml` profile defines the build behavior, while the `settings.xml` profile usually provides machine-specific configurations like repositories, credentials, and mirrors.

---

### Profile structure inside `pom.xml`

```xml
<profiles>
    <profile>
        <id>production</id>

        <properties>
            <java.version>21</java.version>
            <spring.profiles.active>prod</spring.profiles.active>
        </properties>

        <dependencies>
            <dependency>
                <groupId>org.postgresql</groupId>
                <artifactId>postgresql</artifactId>
                <scope>runtime</scope>
            </dependency>
        </dependencies>

        <build>
            <plugins>
                <plugin>
                    <groupId>org.springframework.boot</groupId>
                    <artifactId>spring-boot-maven-plugin</artifactId>
                </plugin>

                <plugin>
                    <groupId>org.apache.maven.plugins</groupId>
                    <artifactId>maven-surefire-plugin</artifactId>

                    <configuration>
                        <skipTests>false</skipTests>
                    </configuration>
                </plugin>
            </plugins>

        </build>
    </profile>
</profiles>
```

---

### Profile structure inside `settings.xml`

```xml
<settings>
    <profiles>
        <profile>

            <id>production</id>

            <repositories>
                <repository>
                    <id>company-public</id>
                    <url>https://repo.example.com/repository/maven-public/</url>
                </repository>
            </repositories>

            <pluginRepositories>
                <pluginRepository>
                    <id>company-plugins</id>
                    <url>https://repo.example.com/repository/maven-plugins/</url>
                </pluginRepository>
            </pluginRepositories>

            <properties>
                <deployment.environment>production</deployment.environment>
            </properties>

        </profile>
    </profiles>

    <activeProfiles>
        <activeProfile>production</activeProfile>
    </activeProfiles>

</settings>
```

---

### Profile tag breakdown

#### profiles

Container that holds all Maven profiles.

```xml
<profiles>...</profiles>
```

---

#### profile

Defines a single environment-specific configuration.

```xml
<profile>
    <id>production</id>
</profile>
```

---

#### id

Unique identifier used to activate a specific profile.

```xml
<id>production</id>
```

---

#### properties

Defines variables used only when the profile is active.

This allows different environments to use different configuration values during builds.

```xml
<properties>
    <spring.profiles.active>prod</spring.profiles.active>
</properties>
```

---

#### dependencies

Defines environment-specific dependencies.

Example:
- development profile → debugging libraries
- production profile → production database drivers

```xml
<dependencies>...</dependencies>
```

---

#### build

Defines environment-specific build behavior.

```xml
<build>...</build>
```

---

#### plugins

Defines plugins Maven should use when the profile is active.

```xml
<plugins>...</plugins>
```

---

#### repositories

Defines repositories Maven should use to download dependencies for that environment.

```xml
<repositories>...</repositories>
```

---

#### pluginRepositories

Defines repositories Maven should use when downloading Maven plugins.

```xml
<pluginRepositories>...</pluginRepositories>
```

---

#### activeProfiles

Automatically activates selected profiles during Maven builds.

```xml
<activeProfiles>
    <activeProfile>production</activeProfile>
</activeProfiles>
```

---

### How pom.xml and settings.xml profiles work together

The `pom.xml` profile controls the application's build behavior such as:
- dependencies
- plugins
- properties
- packaging behavior

The `settings.xml` profile controls the local machine or organization-specific environment such as:
- internal repositories
- plugin repositories
- mirrors
- credentials
- deployment environments

This separation keeps sensitive infrastructure configuration outside the project source code while still allowing Maven to build consistently across environments.

---

## Maven Repositories

Repositories are storage locations where Maven downloads dependencies and plugins from, or uploads built artifacts to.

Maven repositories are a major part of the dependency management system because they allow projects to reuse already built libraries instead of rewriting everything from scratch.

Maven mainly works with three repository types:
- Local repository
- Central repository
- Remote repositories

---

### Local repository (`~/.m2/repository`)

The local repository is Maven’s cache stored inside the user's home directory.

```bash
~/.m2/repository
```

When Maven downloads a dependency or plugin for the first time, it stores a local copy inside the `.m2/repository` directory.

The next time the same dependency is needed, Maven retrieves it directly from the local repository instead of downloading it again from the internet.

This helps:
- speed up builds
- reduce network usage
- support offline builds
- improve dependency reuse across projects

The local repository is automatically managed by Maven.

---

### Maven Central Repository

The Maven Central Repository is the default public repository Maven communicates with when downloading dependencies and plugins.

It contains thousands of open-source Java libraries published by developers and organizations worldwide.

When a dependency is listed inside the `pom.xml`, Maven first checks:
1. the local repository (`.m2`)
2. then Maven Central if the dependency is missing locally

Maven Central is the main source for most public Java dependencies.

---

### Remote repositories

Remote repositories are external repositories hosted outside the local machine.

These repositories are commonly used by organizations to:
- store internal company libraries
- manage production artifacts
- control dependency access
- scan artifacts for vulnerabilities
- improve dependency management across teams

Common remote repository managers include:
- Nexus Repository
- JFrog Artifactory
- GitHub Packages
- AWS CodeArtifact

Unlike Maven Central, remote repositories can contain:
- private company dependencies
- internal plugins
- production release artifacts
- snapshot versions

Remote repositories are usually configured inside:
- `pom.xml`
- `settings.xml`
- company CI/CD pipelines

---

### Maven repository resolution flow

When Maven needs a dependency, it follows this order:

1. Local repository (`~/.m2/repository`)
2. Remote repositories or mirrors
3. Maven Central Repository

Once downloaded, the dependency is cached locally for future builds.

---

# Logging and Configuration

Logging and configuration are what make an application observable and environment-aware. Without them, you are essentially flying blind in production since you have no way of knowing what the application is doing, what went wrong, or how it is behaving across different environments.

In Maven-based Spring Boot projects, logging and configuration are managed through resource files, dependencies, profiles, and environment variables. Getting these right is not just a developer concern it directly affects how systems are monitored, debugged, and deployed in real DevOps workflows.

---

## Logging

### What logging is

Logging is the process of recording application events during runtime. Every time something meaningful happens inside the application a request comes in, a database query runs, an error occurs, logging captures that moment and writes it somewhere useful.

The naive approach is `System.out.println()`. It works locally, but it falls apart completely in production. It has no log levels, no filtering, no timestamps, no way to redirect output to files or centralized systems, and no way to turn it on or off without changing code. Production systems need something structured, controllable, and observable which is exactly what logging frameworks provide.

---

### The logging architecture — SLF4J and Logback

Spring Boot uses a two-layer logging setup. Understanding why this exists matters more than just knowing what the layers are.

**SLF4J (Simple Logging Facade for Java)** is the abstraction layer. Your application code talks to SLF4J — not to any specific logging engine. This means if you ever need to swap the underlying logging implementation, your application code does not need to change at all. SLF4J is the interface. It defines what logging should look like from the code's perspective.

**Logback** is the implementation — the actual engine doing the work. It receives the log events from SLF4J and decides where they go, what format they appear in, and which ones get filtered out based on level. Spring Boot includes Logback by default through `spring-boot-starter-logging`, so most projects do not need to add it manually.

The dependency that wires this together:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-logging</artifactId>
</dependency>
```

If you are already using `spring-boot-starter-web`, this is pulled in automatically as a transitive dependency.

---

### Log levels

Log levels define the severity and purpose of a log message. They also act as filters when you set a level, you only see messages at that level and above.

| Level | When to use it |
|---|---|
| TRACE | Extremely detailed internal flow — rarely used outside deep debugging sessions |
| DEBUG | Diagnostic info for developers. Example: `Variable userId is currently 4821` |
| INFO | General progress and normal operations. Example: `Theme Park API started on port 8080` |
| WARN | Something unusual happened but the app is still running. Example: `Database connection slow (2s delay)` |
| ERROR | A specific action failed. Example: `Could not save Ticket ID #502` |
| FATAL | The application is dying. Example: `Out of Memory: Shutting down` |

In development, `DEBUG` gives you the full picture. In production, `WARN` or `ERROR` reduces noise and focuses attention on real problems.

---

### Logger implementation in code

This is how logging looks inside a real Spring Boot controller. The logger is tied to the class it lives in, which makes it easy to trace exactly where a log message came from.

```java
package com.example.greeting.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
public class GreetingController {

    private static final Logger logger =
            LoggerFactory.getLogger(GreetingController.class);

    @GetMapping("/greet")
    public String greet(@RequestParam(defaultValue = "World") String name) {

        logger.info("Received greeting request for user: {}", name);

        return "Hello, " + name + "!";
    }
}
```

`LoggerFactory.getLogger(GreetingController.class)` creates a logger scoped to this specific class. When the log message appears, it will include the class name, making it easy to locate in large codebases.

---

## Logback configuration

### Where the configuration file lives

Logback behavior is controlled through a configuration file placed in:

```
src/main/resources/logback.xml
```

This file is automatically picked up by Spring Boot at startup. It tells Logback where logs should go, what format they should appear in, and which packages should log at which levels.

---

### The three pillars of logback.xml

Every `logback.xml` is built around three core concepts:

**Appenders** define *where* logs go — the console, a file, a remote server, or a cloud shipper like Fluentd.

**Encoders/Layouts** define *what* logs look like — the pattern string that controls what metadata appears on every line.

**Loggers** define *who* logs what — fine-grained control that lets you set `com.myapp` to `DEBUG` while keeping `org.springframework` at `WARN` to reduce framework noise.

---

### logback.xml structure

```xml
<configuration>

  <property name="LOGS" value="./logs" />

  <!-- Console Appender: logs to terminal, best for local dev and Docker -->
  <appender name="Console"
    class="ch.qos.logback.core.ConsoleAppender">
    <layout class="ch.qos.logback.classic.PatternLayout">
      <Pattern>
        %d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n
      </Pattern>
    </layout>
  </appender>

  <!-- RollingFileAppender: writes to disk and rotates files automatically -->
  <appender name="RollingFile"
    class="ch.qos.logback.core.rolling.RollingFileAppender">
    <file>${LOGS}/spring-boot-logger.log</file>
    <encoder
      class="ch.qos.logback.classic.encoder.PatternLayoutEncoder">
      <Pattern>%d %p %C{1.} [%t] %m%n</Pattern>
    </encoder>

    <!-- Rolls the file daily AND whenever it hits 10MB -->
    <rollingPolicy
      class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
      <fileNamePattern>${LOGS}/archived/spring-boot-logger-%d{yyyy-MM-dd}.%i.log
      </fileNamePattern>
      <timeBasedFileNamingAndTriggeringPolicy
        class="ch.qos.logback.core.rolling.SizeAndTimeBasedFNATP">
        <maxFileSize>10MB</maxFileSize>
      </timeBasedFileNamingAndTriggeringPolicy>
    </rollingPolicy>
  </appender>

  <!-- Global default: everything at INFO goes to Console -->
  <root level="info">
    <appender-ref ref="Console" />
  </root>

  <!-- Package-specific override: com.plantplaces logs at TRACE level -->
  <logger name="com.plantplaces" level="trace" additivity="false">
    <appender-ref ref="RollingFile" />
    <appender-ref ref="Console" />
  </logger>

</configuration>
```

---

### Logback tag breakdown

#### configuration

The root container. Everything lives inside this tag.

```xml
<configuration>...</configuration>
```

---

#### appender

Defines where logs are sent. You can have multiple appenders active at the same time — for example, one sending to the console and another writing to a rolling file.

```xml
<appender name="Console"
          class="ch.qos.logback.core.ConsoleAppender">
```

The three most important appender types:

- **ConsoleAppender** → sends logs to `System.out`. Best for local development and Docker containers, because Docker captures stdout and makes it available through `docker logs`.
- **FileAppender** → writes logs directly to a file. Simple but does not handle file size growth.
- **RollingFileAppender** → the production-grade option. Automatically creates new log files based on size (e.g. every 10MB) or time (e.g. every day). Prevents the server disk from filling up by archiving and deleting old files based on a retention policy.

---

#### encoder

Defines the format of every log line.

```xml
<encoder>
    <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
</encoder>
```

---

#### pattern — what each token means

The pattern string controls exactly what metadata appears on every log line. Breaking down the standard production pattern:

| Token | What it outputs |
|---|---|
| `%d{yyyy-MM-dd HH:mm:ss}` | Timestamp — essential for knowing *when* things broke |
| `[%thread]` | Which thread ran the code — critical for debugging multi-threaded apps |
| `%-5level` | The log level (INFO, ERROR, etc.), padded to 5 characters for neat alignment |
| `%logger{36}` | The class name the log came from |
| `%msg` | The actual message written in the code |
| `%n` | Newline |

---

#### root

Sets the global default log level for the entire application.

```xml
<root level="INFO">
    <appender-ref ref="CONSOLE"/>
</root>
```

---

#### logger

Provides fine-grained control at the package level. This is how you tell Logback to treat your own application code differently from third-party framework code.

```xml
<logger name="com.plantplaces" level="trace" additivity="false">
    <appender-ref ref="RollingFile" />
    <appender-ref ref="Console" />
</logger>
```

`additivity="false"` prevents the log event from bubbling up to the root logger and being printed twice.

---

#### ThresholdFilter

Filters logs by level inside a specific appender. Useful when you want a file appender to only capture `WARN` and above, while the console still shows everything.

```xml
<filter class="ch.qos.logback.classic.filter.ThresholdFilter">
    <level>WARN</level>
</filter>
```

---

## Application configuration

### What configuration is

Configuration defines how the application behaves in a given environment. Instead of hardcoding values directly in source code, production applications externalize configuration into files and environment variables. This makes the same codebase deployable across development, testing, and production without changing a single line of code.

Common things that get configured externally:
- server ports
- database connection details
- API keys
- logging levels
- active environment profiles
- external service URLs

---

### application.properties

Spring Boot's primary configuration file lives at:

```
src/main/resources/application.properties
```

```properties
spring.application.name=greeting-service

server.port=9090

logging.level.root=INFO

management.endpoints.web.exposure.include=health,info

spring.profiles.active=dev
```

---

### Common configuration properties

| Property | Purpose |
|---|---|
| `server.port` | Defines the port the application runs on |
| `logging.level.root` | Sets the global logging level |
| `spring.application.name` | Names the application — useful in logs and monitoring |
| `spring.profiles.active` | Activates an environment-specific profile |
| `management.endpoints.web.exposure.include` | Exposes monitoring endpoints (e.g. health checks) |

---

## Environment-specific configuration with Maven Profiles

You do not want the same configuration running locally and in production. In development, you want `DEBUG` level logs, readable plain text, and the console. In production, you want `WARN` or `ERROR` level logs, `RollingFileAppender` or a log shipper, and JSON format so centralized tools like ELK or Splunk can parse them efficiently.

Maven Profiles let a single project carry different behaviors for each environment without touching the main configuration.

---

### Environment-specific properties files

Spring Boot supports named profile files that activate automatically:

- `application-dev.properties` → development settings
- `application-test.properties` → test environment settings
- `application-prod.properties` → production settings

When `spring.profiles.active=prod` is set, Spring Boot loads `application-prod.properties` automatically on top of the base `application.properties`.

---

### Profile structure in pom.xml

```xml
<profiles>
  <profile>
    <id>production</id>
    <properties>
      <spring.profiles.active>prod</spring.profiles.active>
    </properties>
    <build>
      <plugins>
        <plugin>
          <groupId>org.apache.maven.plugins</groupId>
          <artifactId>maven-surefire-plugin</artifactId>
          <configuration>
            <skipTests>false</skipTests>
          </configuration>
        </plugin>
      </plugins>
    </build>
  </profile>
</profiles>
```

Activate a profile at build time:

```bash
mvn clean package -P production
```

---

### Development vs production logging — a practical comparison

| Concern | Development | Production |
|---|---|---|
| Log level | `DEBUG` | `WARN` or `ERROR` |
| Destination | Console (readable in terminal) | RollingFileAppender or SocketAppender |
| Format | Plain text (human-readable) | JSON (machine-parseable for ELK/Splunk) |
| Activation | Default local run | `-P production` or CI/CD pipeline |

---

## Environment variables

Sensitive values like database credentials, API tokens, and passwords must never live inside `pom.xml`, source code, or committed configuration files. Production systems inject these values at runtime through environment variables.

```properties
spring.datasource.username=${DB_USER}
spring.datasource.password=${DB_PASSWORD}
```

When the application starts, Spring Boot resolves `${DB_USER}` from the system environment. The actual secret never appears in the repository. This is the foundation of secure configuration management and integrates cleanly with secret management tools used in CI/CD pipelines.

---

## The DevOps "Big Three" logging strategies

Beyond basic setup, production logging in DevOps environments focuses on three operational concerns.

**Log Rotation and Retention** — `RollingFileAppender` automatically rotates log files based on size or time, and deletes old archives based on a retention policy. Without this, logs will eventually fill up a server's disk and crash the application.

**Structured Logging (JSON)** — Plain text logs are readable to humans but difficult for machines to parse at scale. JSON-formatted logs are what ELK (Elasticsearch, Logstash, Kibana), EFK (Elasticsearch, Fluentd, Kibana), and PLG (Promtail, Loki, Grafana) stacks expect. When every log line is a valid JSON object, querying and filtering across millions of events becomes fast and reliable.

**Contextual Logging with MDC (Mapped Diagnostic Context)** — MDC lets you inject values like a `traceId` or `userId` into every single log line produced during a request, without changing every log statement in the codebase. This means you can filter all logs belonging to one specific user's journey through a complex distributed system, which is critical when debugging production issues that involve multiple services.

---

## Log shipping and centralization

In containerized production systems, logs do not just sit on disk. They move through a pipeline from the container to a place where engineers can search, visualize, and alert on them.

Docker containers are designed to log to `stdout`. Docker captures it, and a log shipper collects it and sends it to a centralized platform. The three most common stacks:

- **ELK** → Elasticsearch stores and indexes logs. Logstash processes and transforms them. Kibana provides the visual interface.
- **EFK** → Replaces Logstash with Fluentd, which is lighter and often preferred in Kubernetes environments.
- **PLG** → Promtail collects logs. Loki stores them. Grafana visualizes them. Common in teams already using Prometheus for metrics.

---

## Best practices and what to avoid

Log exception stack traces without them, an `ERROR` log tells you something failed but not where or why.

Do not log sensitive data passwords, credit card numbers, tokens, and personally identifiable information must never appear in logs. This is both a security requirement and often a legal one.

Do not over-log logging inside tight loops or on every iteration of a high-frequency process will degrade application performance and produce logs that are impossible to read. Log meaningful events, not noise.

---

## How it all connects

When everything is wired correctly, logging and configuration work together as a system. The `logback.xml` controls what gets logged and where. The `application.properties` and profile files control how the application behaves in each environment. Environment variables keep secrets out of the repository. Maven Profiles activate the right configuration for the right deployment target. And in production, a log shipper moves those logs into a centralized platform where teams can actually observe what the system is doing.

