# Plan de Implementación: Reemplazo de Webhooks REST por Kafka (Notificaciones y Alertas)

## Estado Actual
- Kafka confirmado corriendo: contenedor `yaku-kafka` (`apache/kafka:latest`), puerto `9092`.
- Bootstrap servers:
  - Entre contenedores Docker: `kafka:9092`
  - Local telemetry dev: `localhost:9092`
  - Local notification dev: `localhost:9093`

## Contexto y Objetivo
Actualmente, el microservicio `telemetry-service` alerta sobre umbrales excedidos enviando notificaciones al `notification-service` mediante llamadas HTTP síncronas (Webhooks vía `RestTemplate`).

Para alinear el sistema con una verdadera **Arquitectura Orientada a Eventos (EDA)** y hacer que Kafka sea el CORE, el objetivo es **eliminar el acoplamiento HTTP** y reemplazarlo por la publicación/consumo de eventos a través de un topic de Kafka llamado `telemetry.alerts`.

## Decisiones de Diseño Confirmadas
| Pregunta | Decisión |
|----------|----------|
| ¿Kafka está corriendo? | Sí. |
| ¿Webhook REST? | **Eliminarlo completamente** (`NotificationWebhookController.java`). No deprecated. |
| ¿Orden estricto vs paralelo? | **Procesamiento paralelo**. No se garantiza orden de llegada entre alertas del mismo usuario. |
| ¿Kafka caído? | **Fire-and-forget simple**: loggear el error y continuar. Se acepta pérdida ocasional de alertas. |
| ¿Mensajes fallidos / DLQ? | **Solo logging**. No se implementa Dead Letter Topic por ahora. |

### Notas sobre las decisiones
- **Procesamiento paralelo**: Kafka puede entregar mensajes de diferentes particiones en paralelo. Si no necesitas que la notificación "crítica" siempre llegue después de la "advertencia", esto es más rápido y escalable. Si en el futuro necesitas orden por usuario, volveríamos a usar `userId` como key de partición.
- **Kafka caído (fire-and-forget)**: es la opción más simple. El servicio de telemetry no se bloquea si Kafka no responde. El costo es que, si Kafka está abajo justo en ese momento, esa alerta se pierde.
- **Sin DLQ**: un mensaje mal formado se loguea y se descarta. No se reintenta ni se guarda en otro topic.

---

## Instrucciones para la IA Implementadora

Debes trabajar sobre dos microservicios principales: `telemetry-service` y `notification-service`.

### Fase 1: Refactorización en `telemetry-service`
**Objetivo:** Dejar de usar `RestTemplate` y comenzar a publicar las alertas (`ThresholdBreachedEvent`) hacia Kafka.

1. **Renombrar y modificar `NotificationWebhookService.java`** (ubicado en `application/internal/outboundservices/`):
   - **[DELETE]** Eliminar el objeto `RestTemplate` y la constante de la URL (`http://localhost:8083/api/v1/webhooks/notifications`).
   - **[NEW]** Renombrar la clase a `TelemetryAlertKafkaPublisher`.
   - Inyectar `KafkaTemplate<String, String>` y `ObjectMapper`.
   - En el método `@EventListener public void on(ThresholdBreachedEvent event)`, serializar el evento a JSON usando un DTO/record compartido (mismos campos que `SendNotificationRequestResource`: `type`, `message`, `userId`, `value`, `sensorType`, `hardwareStatus`).
   - **[NEW]** Publicar el JSON usando:
     ```java
     kafkaTemplate.send("telemetry.alerts", jsonPayload)
         .whenComplete((result, ex) -> {
             if (ex != null) {
                 log.error("Failed to send alert to Kafka", ex);
             }
         });
     ```
   - No usar key por ahora (procesamiento paralelo).

2. **Configuración de Kafka Producer**:
   - Reutilizar o crear `KafkaProducerConfig` en `infrastructure/events/kafka` si no existe.
   - Verificar que `application.yml` / `application.properties` tenga:
     ```yaml
     spring:
       kafka:
         bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
     ```

### Fase 2: Refactorización en `notification-service`
**Objetivo:** Reemplazar el Webhook Controller para que las notificaciones se creen en base a los mensajes consumidos de Kafka.

1. **Crear Consumidor `TelemetryAlertsKafkaConsumer.java`**:
   - **[NEW]** Crear esta nueva clase en `notification_service/notification/interfaces/events/`.
   - Añadirle la anotación `@Component`.
   - Crear un método con:
     ```java
     @KafkaListener(topics = "telemetry.alerts", groupId = "notification-group")
     ```
   - Inyectar `ObjectMapper` y `SendNotificationCommandService`.
   - Deserializar el payload JSON en un DTO local (reutilizar/clonar `SendNotificationRequestResource`).
   - Mapear el DTO a `SendNotificationCommand` y llamar a `sendNotificationCommandHandler.handle(command)`.
   - **⚠️ Importante:** Envolver la lógica en `try-catch`. Si falla la deserialización o el manejo, loggear el error y el payload, **no relanzar la excepción** para no bloquear el consumer.

2. **Eliminar el Webhook REST**:
   - **[DELETE]** Eliminar `NotificationWebhookController.java` (`/api/v1/webhooks/notifications`) completamente. No deprecated.

3. **Configuración de Kafka Consumer**:
   - Reutilizar o crear `KafkaConsumerConfig` si no existe.
   - Verificar `application.yml`:
     ```yaml
     spring:
       kafka:
         bootstrap-servers: ${SPRING_KAFKA_BOOTSTRAP_SERVERS:localhost:9093}
         consumer:
           group-id: notification-group
           auto-offset-reset: earliest
     ```

### Consideraciones Técnicas
- **DTOs compartidos:** Definir un record/POJO común (o duplicado idéntico) para el payload JSON. Campos mínimos: `type`, `message`, `userId`, `value`, `sensorType`, `hardwareStatus`.
- **Topic:** `telemetry.alerts`. Kafka tiene `auto.create.topics.enable=true`, así que se creará automáticamente. Considerar definirlo explícitamente si se quiere controlar particiones/replicación.
- **Procesamiento paralelo:** Como no usamos key, las alertas se distribuyen entre particiones y se procesan en paralelo. No hay garantía de orden entre alertas del mismo usuario.
- **Manejo de errores:** Solo logging. No retries configurados, no DLQ.
- **Kafka caído:** Fire-and-forget. `kafkaTemplate.send` es asíncrono; si falla, se loguea y el flujo de telemetry continúa.

---

## Preguntas Abiertas / Siguientes Decisiones
1. **¿DTO compartido o duplicado?**  
   ¿Creamos un módulo/record compartido entre `telemetry-service` y `notification-service`, o duplicamos la estructura en cada uno?

2. **¿Cuántas particiones para `telemetry.alerts`?**  
   El topic se crea automáticamente con el default (`num.partitions=1`). ¿Queremos múltiples particiones para aprovechar el paralelismo, o dejamos 1 por simplicidad?

3. **¿Dónde se publica exactamente en `telemetry-service`?**  
   ¿El `KafkaTemplate` se usa dentro del `@EventListener` actual de `ThresholdBreachedEvent`, o preferimos invocar el publisher directamente desde el servicio de thresholds?

4. **¿Tests?**  
   Los tests actuales usan H2 y deshabilitan Kafka. ¿Queremos agregar tests de integración con Kafka embebido/Testcontainers, o solo un test unitario del consumer/producer?

5. **¿Idempotencia / duplicados?**  
   Kafka puede entregar el mismo mensaje más de una vez (at-least-once). ¿Nos preocupa recibir notificaciones duplicadas, o aceptamos que el usuario pueda ver la misma alerta 2 veces ocasionalmente?

6. **¿Bootstrap servers en local vs Docker?**  
   En Docker es `kafka:9092`, pero en local dev es `localhost:9092` (telemetry) o `localhost:9093` (notification). ¿Ya manejas esto con variables de entorno o perfiles?

7. **¿Eliminar también `RestTemplate` bean/config?**  
   Si `RestTemplate` solo se usaba para el webhook, ¿eliminamos su configuración también, o se conserva por si se necesita en otro lugar?
