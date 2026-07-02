package io.github.rafaviv.yakubackend.equipment.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.commands.CreateFarmCommand;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetFarmByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetFarmsByOwnerIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.services.FarmCommandService;
import io.github.rafaviv.yakubackend.equipment.domain.services.FarmQueryService;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.resources.CreateFarmResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.resources.FarmResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.stream.Collectors;
@RestController
@RequestMapping("/api/v1/farms")
public class FarmController {

    private final FarmCommandService farmCommandService;
    private final FarmQueryService farmQueryService;

    public FarmController(FarmCommandService farmCommandService, FarmQueryService farmQueryService) {
        this.farmCommandService = farmCommandService;
        this.farmQueryService = farmQueryService;
    }

    @PostMapping
    public ResponseEntity<FarmResource> createFarm(@RequestBody CreateFarmResource resource, @RequestHeader("X-User-Id") Long ownerId) {
        Long adminId = ownerId;
        
        CreateFarmCommand command = new CreateFarmCommand(resource.name(), adminId, resource.address());
        var farm = farmCommandService.handle(command);
        if (farm.isEmpty()) return ResponseEntity.badRequest().build();
        
        var createdFarm = farm.get();
        var farmResource = new FarmResource(createdFarm.getId(), createdFarm.getName(), createdFarm.getOwnerId(), createdFarm.getAddress(), createdFarm.getFarmToken());

        return new ResponseEntity<>(farmResource, HttpStatus.CREATED);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteFarm(@PathVariable Long id) {
        try {
            farmCommandService.deleteFarm(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }



    @GetMapping
    public ResponseEntity<List<FarmResource>> getAllFarmsByOwner(@RequestHeader("X-User-Id") Long ownerId) {
        Long adminId = ownerId;
        
        var query = new GetFarmsByOwnerIdQuery(adminId);
        var farms = farmQueryService.handle(query);
        var resources = farms.stream()
                .map(farm -> new FarmResource(farm.getId(), farm.getName(), farm.getOwnerId(), farm.getAddress(), farm.getFarmToken()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FarmResource> getFarmById(@PathVariable Long id) {
        var query = new GetFarmByIdQuery(id);
        var farm = farmQueryService.handle(query);
        
        if (farm.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        
        var foundFarm = farm.get();
        var farmResource = new FarmResource(foundFarm.getId(), foundFarm.getName(), foundFarm.getOwnerId(), foundFarm.getAddress(), foundFarm.getFarmToken());
        
        return ResponseEntity.ok(farmResource);
    }

    @PatchMapping("/{id}/token")
    public ResponseEntity<FarmResource> regenerateToken(@PathVariable Long id) {
        return farmCommandService.regenerateToken(id)
                .map(farm -> ResponseEntity.ok(new FarmResource(farm.getId(), farm.getName(), farm.getOwnerId(), farm.getAddress(), farm.getFarmToken())))
                .orElse(ResponseEntity.notFound().build());
    }

}