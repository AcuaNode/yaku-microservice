package notification_service.notification.interfaces.rest.resources;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record NotificationResponseResource(
    Long id,
    String type,
    String message,
    Long recipientUserId,
    BigDecimal triggerValue,
    String triggerSensorType,
    String triggerHardwareStatus,
    LocalDateTime createdAt
) {}
