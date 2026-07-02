package io.github.rafaviv.yakubackend.equipment.domain.services;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;

import java.util.Optional;

public interface EquipmentCommandService {
    Optional<Equipment> registerEquipment(EquipmentType type, String name, String physicalCode, Long farmId);
    Optional<Equipment> linkEquipmentToPond(Long equipmentId, Long pondId);
    void deleteEquipment(Long equipmentId);
}
