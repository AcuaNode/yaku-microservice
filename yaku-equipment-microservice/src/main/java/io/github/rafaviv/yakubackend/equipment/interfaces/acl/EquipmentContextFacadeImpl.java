package io.github.rafaviv.yakubackend.equipment.interfaces.acl;

import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import org.springframework.stereotype.Service;

@Service
public class EquipmentContextFacadeImpl implements EquipmentContextFacade {

    private final FarmRepository farmRepository;
    private final io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository equipmentRepository;

    public EquipmentContextFacadeImpl(FarmRepository farmRepository,
                                      io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository equipmentRepository) {
        this.farmRepository = farmRepository;
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public Long getPondIdByDeviceId(String deviceId) {
        return equipmentRepository.findByPhysicalCode(deviceId)
                .map(io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment::getPondId)
                .orElseGet(() -> equipmentRepository.findByPhysicalCode(deviceId + "-TEMP")
                        .map(io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment::getPondId)
                        .orElseThrow(() -> new IllegalArgumentException("Device not found or not linked to any pond: " + deviceId)));
    }
}
