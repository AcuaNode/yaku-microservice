# Agents

## Build & run

- Use `./mvnw` (Maven wrapper), **not** system `mvn`.
- Compile: `./mvnw compile`
- Test: `./mvnw test`
- Package: `./mvnw package`
- Run locally: `./mvnw spring-boot:run`
- OCI image: `./mvnw spring-boot:build-image`
- Skip tests: `./mvnw package -DskipTests`
- No lint, format, or typecheck tools configured.

## Project structure

- **Java 17**, Spring Boot 3.5.14, Spring Cloud Gateway (WebFlux reactive).
- Single-module Maven project. Group: `com.yaku`, artifact: `gateway`.
- Base package: `com.yaku.gateway` (`src/main/java/com/yaku/gateway/`).
- Entrypoint: `GatewayApplication.java` (standard `@SpringBootApplication`).
- Config: `src/main/resources/application.yml` (routes and port defined there, not in properties).
- Default port is **8080**.
- Tests: JUnit 5 + `reactor-test`.
- Lombok is used for boilerplate reduction.

## Quirks

- `mise.toml` is **gitignored** — each developer manages their own Java toolchain. Do not edit or commit it.
- Downstream service ports: equipment `8081`, iam `8082`, notification `8083`, subscription `8084`.
- No Dockerfile, no CI/CD, no filters, no service discovery configured yet.
- Maven wrapper is scripts-only (`distributionType=only-script`).
