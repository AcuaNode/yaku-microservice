package telemetry_service.telemetry.application.internal.outboundservices;

import java.math.BigDecimal;

public record TelemetryAlertPayload(
    String type,
    String message,
    Long userId,
    BigDecimal value,
    String sensorType,
    String hardwareStatus
) {}
