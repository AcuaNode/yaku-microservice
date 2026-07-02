package telemetry_service.telemetry.application.internal.outboundservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import telemetry_service.telemetry.domain.model.events.ThresholdBreachedEvent;

import java.util.HashMap;
import java.util.Map;

@Service
public class NotificationWebhookService {

    private static final Logger log = LoggerFactory.getLogger(NotificationWebhookService.class);
    private final RestTemplate restTemplate;
    private final String notificationServiceUrl = "http://localhost:8083/api/v1/webhooks/notifications";

    public NotificationWebhookService() {
        this.restTemplate = new RestTemplate();
    }

    @Async
    @EventListener
    public void on(ThresholdBreachedEvent event) {
        log.info("Intercepted ThresholdBreachedEvent for pond {}. Forwarding to notification-service...", event.pondId());
        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("type", event.severity());
            payload.put("message", event.message());
            payload.put("userId", event.targetUserId());
            // Optional fields can be null for this specific webhook since it's just an alert message
            payload.put("value", null);
            payload.put("sensorType", null);
            payload.put("hardwareStatus", null);

            restTemplate.postForObject(notificationServiceUrl, payload, Void.class);
            log.info("Successfully forwarded ThresholdBreachedEvent to notification-service for user {}", event.targetUserId());
        } catch (Exception e) {
            log.error("Failed to forward ThresholdBreachedEvent to notification-service: {}", e.getMessage());
        }
    }
}
