package telemetry_service.telemetry.domain.model.commands;

public record ProcessGroupedTelemetryCommand(
        String deviceId,
        Double temperature,
        Double turbidity,
        Double ica
) {
}