package telemetry_service.telemetry.application.internal.outboundservices;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import telemetry_service.telemetry.domain.model.events.ThresholdBreachedEvent;

@Service
public class TelemetryAlertKafkaPublisher {

    private static final Logger log = LoggerFactory.getLogger(TelemetryAlertKafkaPublisher.class);
    private static final String TOPIC = "telemetry.alerts";

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public TelemetryAlertKafkaPublisher(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @Async
    @EventListener
    public void on(ThresholdBreachedEvent event) {
        log.info("Intercepted ThresholdBreachedEvent for pond {}. Publishing to Kafka...", event.pondId());
        try {
            TelemetryAlertPayload payload = new TelemetryAlertPayload(
                    event.severity(),
                    event.message(),
                    event.targetUserId(),
                    null,
                    null,
                    null
            );

            String jsonPayload = objectMapper.writeValueAsString(payload);
            kafkaTemplate.send(TOPIC, jsonPayload)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("Failed to publish ThresholdBreachedEvent to Kafka for user {}", event.targetUserId(), ex);
                        } else {
                            log.info("Successfully published ThresholdBreachedEvent to Kafka for user {}", event.targetUserId());
                        }
                    });
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize ThresholdBreachedEvent to JSON for user {}", event.targetUserId(), e);
        }
    }
}
