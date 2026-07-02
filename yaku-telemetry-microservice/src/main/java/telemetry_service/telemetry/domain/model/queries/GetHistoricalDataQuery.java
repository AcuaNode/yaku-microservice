package telemetry_service.telemetry.domain.model.queries;

import telemetry_service.telemetry.domain.model.valueobjects.TimeFilter;

public record GetHistoricalDataQuery(
        Long pondId,
        TimeFilter timeFilter
) {
}