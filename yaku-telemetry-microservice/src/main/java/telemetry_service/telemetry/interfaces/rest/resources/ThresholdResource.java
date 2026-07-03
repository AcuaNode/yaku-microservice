package telemetry_service.telemetry.interfaces.rest.resources;

public record ThresholdResource(
        Long id,
        String species,
        Double minTemperature,
        Double maxTemperature,
        Double minTurbidity,
        Double maxTurbidity
) {
}