# ======================================
# Stage 1: Builder
# ======================================
FROM gradle:8-jdk21-alpine AS builder

# Set working directory
WORKDIR /app

# Copy Gradle wrapper and config files first (for caching)
COPY gradle gradle
COPY gradlew .
COPY settings.gradle .
COPY build.gradle .

# Download dependencies (cached if no changes)
RUN ./gradlew dependencies --no-daemon

# Copy source code
COPY src src

# Build application (skip tests for faster build)
RUN ./gradlew clean bootJar --no-daemon -x test

# ======================================
# Stage 2: Runtime
# ======================================
FROM eclipse-temurin:21-jre-alpine

# Add metadata
LABEL maintainer="cryschan"
LABEL description="BE-Repository Spring Boot Application"
LABEL version="0.0.1-SNAPSHOT"

# Set working directory
WORKDIR /app

# Create non-root user for security
RUN addgroup -S spring && adduser -S spring -G spring

# Copy JAR from builder stage
COPY --from=builder /app/build/libs/*.jar app.jar

# Change ownership to non-root user
RUN chown spring:spring app.jar

# Switch to non-root user
USER spring:spring

# Expose application port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}", \
  "-jar", \
  "app.jar"]
