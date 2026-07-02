# AGENTS.md — notification-service

## Project basics
- Spring Boot 3.4.3 / Java 17 / Maven single-module project.
- Package root: `notification_service` (underscore). The root `pom.xml` declares `groupId=io.github.rafaviv` but source code is not under that package path.
- No `README`, no CI workflows. A `compose.yaml` exists for local Postgres + Kafka.

## Developer commands
Use the Maven wrapper (Maven 3.9.16):
```bash
./mvnw compile
./mvnw test
./mvnw spring-boot:run
./mvnw package
./mvnw -Dtest=NotificationServiceApplicationTests test
```

## Dev environment defaults
- Active profile is `dev` (`spring.profiles.active=dev` in `application.properties`).
- Dev DB: Postgres at `localhost:5435/yaku_notifications` (user `root` / `password`).
- Dev Kafka: `localhost:9093`.
- App runs on port **8083** (`server.port=8083`).
- `spring-boot-docker-compose` dependency is commented out in `pom.xml`; run `docker compose up -d` manually for local infra.

## Testing
- Test profile `test` uses H2 in-memory database (`application-test.properties`).
- `NotificationServiceApplicationTests` has 10 MockMvc tests covering:
  - Context load smoke test
  - Gateway auth: 403 when `X-User-Id` header is missing
  - Gateway auth: 403 when `X-User-Id` header mismatches path variable
  - Gateway auth: 200/201 when header matches path variable
- Kafka auto-configuration is disabled in test profile to avoid broker connection.

## Architecture notes
- DDD-lite folder structure: `domain` / `application/internal` / `infrastructure` / `interfaces/rest`.
- `Notification` and `DeviceToken` are domain aggregates (plain Java objects). JPA entities are `NotificationEntity` and `DeviceTokenEntity` in `infrastructure/persistance/jpa`.
- FCM push notifications are mocked: `FcmClient` implements `PushNotificationService` and only prints to stdout.
- Kafka event publishing: `KafkaDomainEventPublisher` publishes events to Kafka topics using `KafkaTemplate`.
- OpenAPI/Swagger is auto-configured via `springdoc-openapi-starter-webmvc-ui` 2.8.8; config bean reads from `documentation.application.*` properties populated by Maven resource filtering.
- Gateway auth: Gateway-facing controllers read `X-User-Id` via `@RequestHeader(value = "X-User-Id", required = false)` and validate it against the `@PathVariable Long userId`; mismatches (or missing headers) return 403. The `required = false` is intentional — it lets the controller return 403 instead of Spring throwing a 400 for missing headers. The webhook endpoint (`/api/v1/webhooks/notifications`) is service-to-service and does not use gateway headers.

## Repo-specific conventions
- Lombok is used (`@Getter`, `@Setter`) and must be registered as an annotation processor (already configured in `pom.xml`).
- Resource classes use Java records for REST request/response payloads where possible.
- REST base path: `/api/v1`.

## Known issues (see `BUGS.md`)
- `DeviceTokenEntity` has no unique constraint on `(userId, fcmToken)`.
- Application services are not annotated with `@Transactional`.
- `NotificationsController` directly injects `NotificationJpaRepository` instead of using an application command service.
- `NotificationResponseResource` is missing `isRead` and `recipientRole`.
- Request records lack Bean Validation constraints; controllers don't use `@Valid`.
- `GlobalExceptionHandler` only catches `IllegalArgumentException` and `IllegalStateException`.
