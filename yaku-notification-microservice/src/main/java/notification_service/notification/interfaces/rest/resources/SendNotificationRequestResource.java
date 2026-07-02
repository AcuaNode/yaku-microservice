package notification_service.notification.interfaces.rest.resources;

import java.math.BigDecimal;

public record SendNotificationRequestResource(
    String type,
    String message,
    Long userId,
    BigDecimal value,
    String sensorType,
    String hardwareStatus
) {}
