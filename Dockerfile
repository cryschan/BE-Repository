# ======================================
# Simplified Dockerfile for 5-person Dev Team
# Optimized for fast builds and local development
# ======================================
FROM gradle:8-jdk21-alpine

# Set working directory
WORKDIR /app

# Copy entire project
COPY . .

# Build application (skip tests for faster builds)
RUN ./gradlew clean bootJar --no-daemon -x test

# Expose application port
EXPOSE 8080

# Run application with environment-based profile
ENTRYPOINT ["java", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-default}", \
  "-jar", \
  "build/libs/be-repository-0.0.1-SNAPSHOT.jar"]
