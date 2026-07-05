package io.github.rafaviv.yakubackend.equipment.interfaces.acl;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import io.github.rafaviv.yakubackend.telemetry.application.outboundservices.acl.ExternalEquipmentService;
import org.springframework.stereotype.Component;

/**
 * Adapter that implements the ExternalEquipmentService port defined in the Telemetry context.
 * Serves as an ACL facade for Equipment context.
 */
@Component
public class PondSpeciesFacadeAdapter implements ExternalEquipmentService {

    private final PondRepository pondRepository;
    private final FarmRepository farmRepository;

    public PondSpeciesFacadeAdapter(PondRepository pondRepository, FarmRepository farmRepository) {
        this.pondRepository = pondRepository;
        this.farmRepository = farmRepository;
    }

    @Override
    public String getSpeciesByPondId(Long pondId) {
        Pond pond = pondRepository.findById(pondId)
                .orElseThrow(() -> new IllegalArgumentException("Pond with ID " + pondId + " not found"));
        
        if (pond.getSpecies() == null) {
            throw new IllegalStateException("Pond with ID " + pondId + " does not have an assigned species");
        }
        
        return pond.getSpecies().name();
    }

    @Override
    public Long getUserIdByPondId(Long pondId) {
        Pond pond = pondRepository.findById(pondId)
                .orElseThrow(() -> new IllegalArgumentException("Pond with ID " + pondId + " not found"));
        
        Farm farm = farmRepository.findById(pond.getFarmId())
                .orElseThrow(() -> new IllegalStateException("Farm with ID " + pond.getFarmId() + " not found"));
        
        return farm.getOwnerId();
    }

    @Override
    public Long getOperatorIdByPondId(Long pondId) {
        Pond pond = pondRepository.findById(pondId)
                .orElseThrow(() -> new IllegalArgumentException("Pond with ID " + pondId + " not found"));

        if (pond.getAssignedOperatorId() != null) {
            return pond.getAssignedOperatorId();
        }

        // Fallback to owner
        Farm farm = farmRepository.findById(pond.getFarmId())
                .orElseThrow(() -> new IllegalStateException("Farm with ID " + pond.getFarmId() + " not found"));
        return farm.getOwnerId();
    }
}
