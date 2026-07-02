# API Gateway

Spring Cloud Gateway (WebFlux) service for the Yaku microservices platform.

## Prerequisites

- Java 17 (use [mise](https://mise.jdx.dev) or your preferred toolchain manager)
- Maven (via the included wrapper — no manual install needed)

## Quick start

```bash
./mvnw spring-boot:run
```

The gateway starts on port **8080** by default.

## Downstream Services

The gateway routes to the following microservices:

| Service | Route Prefix | Target |
|---|---|---|
| equipment-service | `/api/v1/farms/**`, `/api/v1/ponds/**`, `/api/v1/equipment/**` | `localhost:8081` |
| iam-service | `/api/v1/users/**` | `localhost:8082` |
| notification-service | `/api/v1/users/*/notifications/**`, `/api/v1/users/*/device-tokens/**`, `/api/v1/webhooks/notifications/**` | `localhost:8083` |
| subscription-service | `/api/v1/subscriptions/**`, `/api/v1/plans/**`, `/api/v1/payments/**`, `/api/v1/webhooks/stripe/**` | `localhost:8084` |

When running the full platform locally, start downstream services on their assigned ports:
```bash
# equipment-service
./mvnw spring-boot:run -Dspring-boot.run.arguments="--server.port=8081"
```

## Build

```bash
./mvnw clean package
```

Produces `target/gateway-0.0.1-SNAPSHOT.jar`.

## Test

```bash
./mvnw test
```

## OCI image

```bash
./mvnw spring-boot:build-image
```

Uses Cloud Native Buildpacks (no Dockerfile required).

## Tech stack

| Component              | Version     |
|------------------------|-------------|
| Java                   | 17          |
| Spring Boot            | 3.5.14      |
| Spring Cloud Gateway   | 2025.0.2    |
| Spring Cloud           | 2025.0.2    |
| Maven                  | 3.9.16      |
| Lombok                 | latest      |
| JUnit                  | 5           |
| Reactor Test           | latest      |
