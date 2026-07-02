# Known Issues — notification-service

## Spring Context / Startup

- `application.properties` previously referenced a missing class (`SnakeCaseWithPluralizedTablePhysicalNamingStrategy`). **FIXED** — class copied into repo.
- `spring.application.name` was `equipment-service`. **FIXED** — now `notification-service`.
- Spring Boot parent POM was `4.0.6` (non-existent version). **FIXED** — downgraded to `3.4.3`.
- Tests required a running database. **FIXED** — `application-test.properties` with H2 in-memory database added.

## Copy-Paste Template Artifacts

- `data.sql` previously contained `equipment-service` header comment. **FIXED**.

## Repository Structure

- A duplicate nested `notification-service/` directory previously existed at the repo root. **FIXED** — removed.

## Local Development

- `compose.yaml` is now provided for local Postgres (`:5435`) + Kafka (`:9093`).
- `spring-boot-docker-compose` dependency is commented out in `pom.xml`; devs must run `docker compose up -d` manually.

## Remaining Functional Issues

1. **`DeviceTokenEntity` lacks a unique constraint on `(userId, fcmToken)`** — concurrent registration requests can create duplicate rows.

2. **No `@Transactional` on application services** — `SendNotificationCommandService`, `RegisterDeviceTokenCommandService`, and repository adapters are not transactional.

3. **`NotificationsController` directly injects `NotificationJpaRepository`** — the `PATCH /read` endpoint bypasses the application layer.

4. **`NotificationResponseResource` is missing `isRead` and `recipientRole`** — these fields exist in the entity but are not exposed in the REST response.

5. **Request records lack validation** — `SendNotificationRequestResource` and `RegisterDeviceTokenRequestResource` have no Bean Validation constraints; controllers don't use `@Valid`.

6. **`GlobalExceptionHandler` only catches `IllegalArgumentException` and `IllegalStateException`** — all other exceptions fall through to Spring's default HTML error page.

7. **`AuditableAbstractAggregateRoot` and `AuditableModel` are unused** — entities don't extend them despite `@EnableJpaAuditing` being present.

8. **`io.github.encryptorcode:pluralize` dependency is dead weight** — only used by the naming strategy class.
