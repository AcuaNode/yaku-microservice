package telemetry_service.telemetry.interfaces.rest.transform;

import telemetry_service.telemetry.domain.model.aggregates.Threshold;
import telemetry_service.telemetry.interfaces.rest.resources.ThresholdResource;

public class ThresholdResourceFromEntityAssembler {
    public static ThresholdResource toResourceFromEntity(Threshold entity) {
        return new ThresholdResource(
                entity.getId(),
                entity.getSpecies() != null ? entity.getSpecies().name() : null,
                entity.getMinTemperature(),
                entity.getMaxTemperature(),
                entity.getMinTurbidity(),
                entity.getMaxTurbidity()
        );
    }
}
