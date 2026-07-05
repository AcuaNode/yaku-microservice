package io.github.rafaviv.yakubackend.equipment.interfaces.rest;

import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentCommandService;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByFarmIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByIdQuery;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.RegisterEquipmentResource;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform.RegisterIoTDeviceResource;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/equipment")
public class EquipmentController {

    private final EquipmentCommandService equipmentCommandService;
    private final io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentQueryService equipmentQueryService;

    public EquipmentController(EquipmentCommandService equipmentCommandService,
            io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentQueryService equipmentQueryService) {
        this.equipmentCommandService = equipmentCommandService;
        this.equipmentQueryService = equipmentQueryService;
    }

    @PostMapping
    public ResponseEntity<?> registerEquipment(@RequestBody RegisterEquipmentResource resource) {
        try {
            var equipment = equipmentCommandService.registerEquipment(
                    EquipmentType.valueOf(resource.type().toUpperCase()), resource.name(), resource.physicalCode(),
                    resource.farmId());
            if (equipment.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(equipment.get());
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(e.getMessage());
        }
    }

    @PostMapping("/iot-device")
    public ResponseEntity<?> registerIoTDevice(@RequestBody RegisterIoTDeviceResource resource) {
        try {
            var equipments = equipmentCommandService.registerIoTDevice(resource.deviceId(), resource.deviceName(),
                    resource.farmId());
            if (equipments.isEmpty()) {
                return ResponseEntity.badRequest().build();
            }
            return ResponseEntity.status(HttpStatus.CREATED).body(equipments);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{equipmentId}/link/{pondId}")
    public ResponseEntity<?> linkEquipmentToPond(@PathVariable Long equipmentId, @PathVariable Long pondId) {
        var equipment = equipmentCommandService.linkEquipmentToPond(equipmentId, pondId);
        if (equipment.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(equipment.get());
    }

    @GetMapping
    public ResponseEntity<java.util.List<io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment>> getAllEquipment(
            @RequestParam(required = false) Long farmId) {
        if (farmId != null) {
            return ResponseEntity.ok(equipmentQueryService.handle(new GetEquipmentByFarmIdQuery(farmId)));
        }
        return ResponseEntity.ok(equipmentQueryService.getAllEquipment());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEquipmentById(@PathVariable Long id) {
        Optional<io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment> equipment = equipmentQueryService
                .handle(new GetEquipmentByIdQuery(id));
        if (equipment.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Equipment not found with id: " + id);
        }
        return ResponseEntity.ok(equipment.get());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEquipment(@PathVariable Long id) {
        try {
            equipmentCommandService.deleteEquipment(id);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{equipmentId}/command")
    public ResponseEntity<?> executeRemoteCommand(@PathVariable Long equipmentId) {
        try {
            equipmentCommandService.executeRemoteCommand(equipmentId);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error executing command");
        }
    }
}
