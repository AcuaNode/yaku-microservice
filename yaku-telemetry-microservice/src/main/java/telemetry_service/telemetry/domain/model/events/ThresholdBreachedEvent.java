package telemetry_service.telemetry.domain.model.events;

public record ThresholdBreachedEvent(
        Long pondId,
        Long targetUserId,
        String severity,
        String message
) {
}
