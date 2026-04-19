# ===== BUILD STAGE =====
FROM eclipse-temurin:21-jdk-alpine AS build

WORKDIR /app

# Copy gradle wrapper and config
COPY gradlew .
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Make gradlew executable
RUN chmod +x gradlew

# Download dependencies (cached layer)
RUN ./gradlew dependencies --no-daemon || true

# Copy source code
COPY src src

# Build the JAR
RUN ./gradlew clean build -x test --no-daemon

# ===== RUN STAGE =====
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copy built JAR from build stage
COPY --from=build /app/build/libs/*.jar app.jar

# Expose port
EXPOSE 8080

# Run app with memory limits for free tier
ENTRYPOINT ["java", "-Xmx400m", "-Xms128m", "-jar", "app.jar"]