# ==============================
# STAGE 1 — Build Environment
# ==============================

FROM maven:3.9.6-eclipse-temurin-21 AS build

WORKDIR /build

# Copy parent pom
COPY pom.xml .

# Copy child module pom
COPY greeting-api/pom.xml greeting-api/pom.xml

# Download dependencies
RUN mvn dependency:go-offline

# Copy source code
COPY greeting-api/src greeting-api/src

# Build only greeting-api module
RUN mvn clean package -pl greeting-api -am -DskipTests


# ==============================
# STAGE 2 — Runtime Environment
# ==============================

FROM eclipse-temurin:21-jre-alpine

WORKDIR /app
RUN mkdir -p app/logs
COPY --from=build /build/greeting-api/target/*.jar app.jar

EXPOSE 9080

ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]
