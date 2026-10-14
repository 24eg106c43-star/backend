# Stage 1: Build the application using Maven and JDK 17
FROM maven:3.9-eclipse-temurin-17 AS builder
WORKDIR /app

# Copy Maven POM and source files
COPY pom.xml .
COPY src ./src

# Build the executable JAR file
RUN mvn clean package -DskipTests

# Stage 2: Runtime environment using lightweight JDK 17 JRE
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy compiled JAR artifact from builder stage
COPY --from=builder /app/target/farmer-direct-1.0.0.jar app.jar

# Expose Spring Boot default port
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
