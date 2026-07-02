package telemetry_service.telemetry.interfaces.rest;

import telemetry_service.telemetry.application.internal.commandservices.TelemetryCommandService;
import telemetry_service.telemetry.application.internal.queryservices.TelemetryQueryService;
import telemetry_service.telemetry.domain.model.queries.GetThresholdBySpeciesQuery;
import telemetry_service.telemetry.interfaces.rest.resources.ConfigureThresholdResource;
import telemetry_service.telemetry.interfaces.rest.resources.ThresholdResource;
import telemetry_service.telemetry.interfaces.rest.transform.ConfigureThresholdCommandFromResourceAssembler;
import telemetry_service.telemetry.interfaces.rest.transform.ThresholdResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/telemetry/thresholds")
public class ThresholdController {

    private final TelemetryCommandService telemetryCommandService;
    private final TelemetryQueryService telemetryQueryService;

    public ThresholdController(TelemetryCommandService telemetryCommandService, TelemetryQueryService telemetryQueryService) {
        this.telemetryCommandService = telemetryCommandService;
        this.telemetryQueryService = telemetryQueryService;
    }

    @PostMapping
    public ResponseEntity<ThresholdResource> configureThreshold(@RequestBody ConfigureThresholdResource resource) {
        var command = ConfigureThresholdCommandFromResourceAssembler.toCommandFromResource(resource);
        telemetryCommandService.handle(command);

        // Since the handle method returns ID but we want to return the whole resource, we can query it again.
        var query = new GetThresholdBySpeciesQuery(resource.species());
        var thresholdOptional = telemetryQueryService.handle(query);

        if (thresholdOptional.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        var thresholdResource = ThresholdResourceFromEntityAssembler.toResourceFromEntity(thresholdOptional.get());
        return new ResponseEntity<>(thresholdResource, HttpStatus.CREATED);
    }

    @GetMapping("/{species}")
    public ResponseEntity<ThresholdResource> getThresholdBySpecies(@PathVariable String species) {
        var query = new GetThresholdBySpeciesQuery(species);
        var thresholdOptional = telemetryQueryService.handle(query);

        if (thresholdOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        var thresholdResource = ThresholdResourceFromEntityAssembler.toResourceFromEntity(thresholdOptional.get());
        return ResponseEntity.ok(thresholdResource);
    }
}