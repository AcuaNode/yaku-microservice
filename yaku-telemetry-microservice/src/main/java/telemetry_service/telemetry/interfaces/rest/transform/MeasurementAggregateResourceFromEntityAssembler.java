package telemetry_service.telemetry.interfaces.rest.transform;

import telemetry_service.telemetry.domain.model.aggregates.MeasurementAggregate;
import telemetry_service.telemetry.interfaces.rest.MeasurementAggregateResource;

public class MeasurementAggregateResourceFromEntityAssembler {
    public static MeasurementAggregateResource toResourceFromEntity(MeasurementAggregate entity) {
        return new MeasurementAggregateResource(
                entity.getId(),
                entity.getPondId(),
                entity.getSensorType().name(),
                entity.getMinValue(),
                entity.getMaxValue(),
                entity.getAverageValue(),
                entity.getPeriodStart(),
                entity.getPeriodEnd()
        );
    }
}
