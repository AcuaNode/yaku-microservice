package telemetry_service.telemetry.interfaces.rest.resources;

public record ConfigureThresholdResource(
        String species,
        Double minTemperature,
        Double maxTemperature,
        Double minTurbidity,
        Double maxTurbidity
) {
}