package telemetry_service.telemetry.interfaces.rest.transform;

import telemetry_service.telemetry.domain.model.aggregates.SensorReading;
import telemetry_service.telemetry.interfaces.rest.SensorReadingResource;

public class SensorReadingResourceFromEntityAssembler {
    public static SensorReadingResource toResourceFromEntity(SensorReading entity) {
        return new SensorReadingResource(
                entity.getId(),
                entity.getPondId(),
                entity.getSensorType().name(),
                entity.getMeasurement().getValue(),
                entity.getMeasurement().getUnit(),
                entity.getTimestamp()
        );
    }
}
