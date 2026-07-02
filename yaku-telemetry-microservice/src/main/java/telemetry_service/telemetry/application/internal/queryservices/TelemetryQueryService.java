package telemetry_service.telemetry.application.internal.queryservices;

import telemetry_service.telemetry.domain.model.aggregates.MeasurementAggregate;
import telemetry_service.telemetry.domain.model.aggregates.SensorReading;
import telemetry_service.telemetry.domain.model.queries.GetHistoricalDataQuery;
import telemetry_service.telemetry.domain.model.queries.GetPondStatusQuery;

import java.util.List;

public interface TelemetryQueryService {
    List<SensorReading> handle(GetPondStatusQuery query);
    List<MeasurementAggregate> handle(GetHistoricalDataQuery query);

    java.util.Optional<telemetry_service.telemetry.domain.model.aggregates.Threshold> handle(telemetry_service.telemetry.domain.model.queries.GetThresholdBySpeciesQuery query);
}
