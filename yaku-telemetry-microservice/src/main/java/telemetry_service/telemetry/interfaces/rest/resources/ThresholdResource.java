package telemetry_service.telemetry.interfaces.rest.resources;

public record ThresholdResource(
        Long id,
        String species,
        Double minTemperature,
        Double maxTemperature,
        Double minPh,
        Double maxPh,
        Double minTurbidity,
        Double maxTurbidity
) {
}