package notification_service.notification.interfaces.events;

import java.math.BigDecimal;

public record TelemetryAlertPayload(
    String type,
    String message,
    Long userId,
    BigDecimal value,
    String sensorType,
    String hardwareStatus
) {}
