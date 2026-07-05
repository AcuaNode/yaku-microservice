package io.github.rafaviv.yakubackend.equipment.domain.services;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;

import java.util.List;
import java.util.Optional;

public interface EquipmentCommandService {
    Optional<Equipment> registerEquipment(EquipmentType type, String name, String physicalCode, Long farmId);

    List<Equipment> registerIoTDevice(String deviceId, String deviceName, Long farmId);

    Optional<Equipment> linkEquipmentToPond(Long equipmentId, Long pondId);

    void deleteEquipment(Long equipmentId);

    void executeRemoteCommand(Long equipmentId);
}
