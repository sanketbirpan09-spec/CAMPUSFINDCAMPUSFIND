# ==============================================================================
# CampusFind — Production Dockerfile for Render / Railway / Container Clouds
# ==============================================================================

# Stage 1: Build the Application with Maven & OpenJDK 17
FROM maven:3.9.6-eclipse-temurin-17 AS builder
WORKDIR /app

# Cache dependencies by copying pom.xml first
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code and package executable JAR
COPY src ./src
RUN mvn clean package -DskipTests -B

# Stage 2: Minimal Runtime Environment
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# Create non-root user for security
RUN groupadd -r spring && useradd -r -g spring spring

# Create uploads directory with appropriate permissions
RUN mkdir -p /app/uploads && chown -R spring:spring /app

USER spring:spring

# Copy built artifact from builder stage
COPY --from=builder --chown=spring:spring /app/target/campusfind-1.0.0.jar app.jar

# Dynamic Port assigned by Render / Cloud platform (default 8080)
ENV PORT=8080
EXPOSE ${PORT}

# Run the Spring Boot application
ENTRYPOINT ["sh", "-c", "java -Dserver.port=${PORT} -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
