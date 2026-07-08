package notification_service.notification.interfaces.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import notification_service.notification.application.internal.commandservices.SendNotificationCommandService;
import notification_service.notification.domain.models.commands.SendNotificationCommand;
import notification_service.notification.domain.models.valueobjects.NotificationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class TelemetryAlertsKafkaConsumer {

    private static final Logger log = LoggerFactory.getLogger(TelemetryAlertsKafkaConsumer.class);
    private static final String TOPIC = "telemetry.alerts";

    private final ObjectMapper objectMapper;
    private final SendNotificationCommandService sendNotificationCommandHandler;

    public TelemetryAlertsKafkaConsumer(ObjectMapper objectMapper,
                                        SendNotificationCommandService sendNotificationCommandHandler) {
        this.objectMapper = objectMapper;
        this.sendNotificationCommandHandler = sendNotificationCommandHandler;
    }

    @KafkaListener(topics = TOPIC, groupId = "notification-group")
    public void consume(String payload) {
        log.info("Received telemetry alert from Kafka: {}", payload);
        try {
            TelemetryAlertPayload alert = objectMapper.readValue(payload, TelemetryAlertPayload.class);
            SendNotificationCommand command = new SendNotificationCommand(
                    NotificationType.valueOf(alert.type().toUpperCase()),
                    alert.message(),
                    alert.userId(),
                    alert.value(),
                    alert.sensorType(),
                    alert.hardwareStatus()
            );
            sendNotificationCommandHandler.handle(command);
            log.info("Successfully processed telemetry alert for user {}", alert.userId());
        } catch (Exception e) {
            log.error("Failed to process telemetry alert from Kafka. Payload: {}", payload, e);
        }
    }
}
