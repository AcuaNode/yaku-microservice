package telemetry_service.telemetry.domain.model.commands;

public record ConfigureThresholdCommand(
        String species,
        Double minTemperature,
        Double maxTemperature,
        Double minTurbidity,
        Double maxTurbidity
) {
}