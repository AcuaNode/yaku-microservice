# Stage 1: Build stage
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# Install dependencies required by the Maven Wrapper
RUN apk add --no-cache curl unzip tar bash

# Copy Maven Wrapper files
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Make mvnw executable and download dependencies (cached layer)
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy the source code
COPY src ./src

# Package the application
RUN ./mvnw package -DskipTests

# Find the built JAR and rename it to app.jar
RUN mv target/*.jar target/app.jar

# Stage 2: Runtime stage
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Install socat for infrastructure port forwarding workaround
RUN apk add --no-cache socat

# Run under a secure non-root user
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

# Copy jar from build stage
COPY --from=build /app/target/app.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "socat TCP-LISTEN:8081,fork,reuseaddr TCP:equipment-service:8080 & socat TCP-LISTEN:8083,fork,reuseaddr TCP:notification-service:8080 & exec java -jar app.jar"]
