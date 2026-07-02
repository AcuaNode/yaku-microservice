package io.github.rafaviv.yakubackend.equipment.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetAllPondsQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondsByAssignedOperatorIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.domain.services.FarmQueryService;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondCommandService;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondQueryService;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.resources.PondResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.CreatePondResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.PondResourceFromEntityAssembler;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping(value = "/api/v1/ponds", produces = MediaType.APPLICATION_JSON_VALUE)
public class PondController {

    private final PondCommandService pondCommandService;
    private final PondQueryService pondQueryService;

    public PondController(PondCommandService pondCommandService, PondQueryService pondQueryService) {
        this.pondCommandService = pondCommandService;
        this.pondQueryService = pondQueryService;
    }

    @PostMapping
    public ResponseEntity<PondResource> createPond(@RequestBody CreatePondResource resource) {
        var pond = pondCommandService.createPond(resource.farmId(), resource.name(), Species.valueOf(resource.species()), resource.volume());
        if (pond.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        var pondResource = PondResourceFromEntityAssembler.toResourceFromEntity(pond.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(pondResource);
    }

    @GetMapping
    public ResponseEntity<List<PondResource>> getAllPonds() {
        var query = new GetAllPondsQuery();
        var ponds = pondQueryService.handle(query);
        var resources = ponds.stream()
                .map(PondResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<PondResource> getPondById(@PathVariable Long id) {
        var query = new GetPondByIdQuery(id);
        var pond = pondQueryService.handle(query);
        if (pond.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        var pondResource = PondResourceFromEntityAssembler.toResourceFromEntity(pond.get());
        return ResponseEntity.ok(pondResource);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePond(@PathVariable Long id) {
        try {
            pondCommandService.deletePond(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/operator/{operatorId}")
    public ResponseEntity<List<PondResource>> getPondsByOperatorId(@PathVariable Long operatorId) {
        var query = new GetPondsByAssignedOperatorIdQuery(operatorId);
        var ponds = pondQueryService.handle(query);
        var resources = ponds.stream()
                .map(PondResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/farm/{farmId}")
    public ResponseEntity<List<PondResource>> getPondsByFarmId(@PathVariable Long farmId) {
        var query = new io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondsByFarmIdQuery(farmId);
        var ponds = pondQueryService.handle(query);
        var resources = ponds.stream()
                .map(PondResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @PutMapping("/{id}")
    public ResponseEntity<PondResource> updatePond(@PathVariable Long id, @RequestBody CreatePondResource resource) {
        return pondCommandService.updatePond(id, resource.name(), Species.valueOf(resource.species()), resource.volume())
                .map(pond -> ResponseEntity.ok(PondResourceFromEntityAssembler.toResourceFromEntity(pond)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{pondId}/assignments")
    public ResponseEntity<PondResource> assignOperator(@PathVariable Long pondId, @RequestBody Map<String, Long> body) {
        Long operatorId = body.get("operatorId");
        if (operatorId == null) return ResponseEntity.badRequest().build();
        return pondCommandService.assignOperator(pondId, operatorId)
                .map(pond -> ResponseEntity.ok(PondResourceFromEntityAssembler.toResourceFromEntity(pond)))
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{pondId}/deassignments/{operatorId}")
    public ResponseEntity<PondResource> deassignOperator(@PathVariable Long pondId, @PathVariable Long operatorId) {
        return pondCommandService.deassignOperator(pondId)
                .map(pond -> ResponseEntity.ok(PondResourceFromEntityAssembler.toResourceFromEntity(pond)))
                .orElse(ResponseEntity.notFound().build());
    }
}