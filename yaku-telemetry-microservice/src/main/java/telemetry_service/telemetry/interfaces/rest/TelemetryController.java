package telemetry_service.telemetry.interfaces.rest;

import telemetry_service.telemetry.application.internal.queryservices.TelemetryQueryService;
import telemetry_service.telemetry.domain.model.aggregates.MeasurementAggregate;
import telemetry_service.telemetry.domain.model.aggregates.SensorReading;
import telemetry_service.telemetry.domain.model.queries.GetHistoricalDataQuery;
import telemetry_service.telemetry.domain.model.queries.GetPondStatusQuery;
import telemetry_service.telemetry.domain.model.valueobjects.TimeFilter;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/telemetry")
public class TelemetryController {

    private final TelemetryQueryService telemetryQueryService;
    private final telemetry_service.telemetry.application.internal.commandservices.TelemetryCommandService telemetryCommandService;

    public TelemetryController(TelemetryQueryService telemetryQueryService, telemetry_service.telemetry.application.internal.commandservices.TelemetryCommandService telemetryCommandService) {
        this.telemetryQueryService = telemetryQueryService;
        this.telemetryCommandService = telemetryCommandService;
    }

    @GetMapping("/ponds/{pondId}/status")
    public ResponseEntity<List<SensorReading>> getPondStatus(@PathVariable Long pondId) {
        List<SensorReading> readings = telemetryQueryService.handle(new GetPondStatusQuery(pondId));
        if (readings == null || readings.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(readings);
    }

    @GetMapping("/ponds/{pondId}/historical")
    public ResponseEntity<List<MeasurementAggregate>> getHistoricalData(
            @PathVariable Long pondId,
            @RequestParam(defaultValue = "WEEKLY") String timeFilter) {
        try {
            TimeFilter filter = TimeFilter.valueOf(timeFilter.toUpperCase());
            List<MeasurementAggregate> aggregates = telemetryQueryService.handle(new GetHistoricalDataQuery(pondId, filter));
            return ResponseEntity.ok(aggregates);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PostMapping("/manual-ingest")
    public ResponseEntity<String> manualIngest(@RequestBody telemetry_service.telemetry.domain.model.commands.ProcessGroupedTelemetryCommand command) {
        try {
            telemetryCommandService.handle(command);
            return ResponseEntity.ok("Telemetry ingested successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}