package io.github.rafaviv.yakubackend.equipment.domain.model.aggregates;

import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentStatus;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EquipmentTest {

    @Test
    @DisplayName("Given valid equipment data, When creating equipment, Then it is created with AVAILABLE status")
    void createEquipment_Successfully() {
        EquipmentType type = EquipmentType.SENSOR;
        String name = "Temperature Sensor";
        String physicalCode = "TS-001";

        Equipment equipment = new Equipment(type, name, physicalCode, null);

        assertEquals(type, equipment.getType());
        assertEquals(name, equipment.getName());
        assertEquals(physicalCode, equipment.getPhysicalCode());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.getStatus());
        assertNull(equipment.getPondId());
        assertNull(equipment.getAddress());
    }

    @Test
    @DisplayName("Given available equipment, When linking to pond, Then status becomes LINKED")
    void linkToPond_Successfully() {
        Equipment equipment = new Equipment(EquipmentType.SENSOR, "Temp Sensor", "TS-001", null);
        Long pondId = 5L;

        equipment.linkToPond(pondId);

        assertEquals(pondId, equipment.getPondId());
        assertEquals(EquipmentStatus.LINKED, equipment.getStatus());
    }

    @Test
    @DisplayName("Given linked equipment, When unlinking, Then status becomes AVAILABLE and pondId is null")
    void unlinkFromPond_Successfully() {
        Equipment equipment = new Equipment(EquipmentType.SENSOR, "Temp Sensor", "TS-001", null);
        equipment.linkToPond(5L);

        equipment.unlinkFromPond();

        assertNull(equipment.getPondId());
        assertEquals(EquipmentStatus.AVAILABLE, equipment.getStatus());
    }

    @Test
    @DisplayName("Given equipment, When relinking to different pond, Then pondId is updated")
    void linkToPond_ReLink_UpdatesPondId() {
        Equipment equipment = new Equipment(EquipmentType.SENSOR, "Temp Sensor", "TS-001", null);
        equipment.linkToPond(5L);

        equipment.linkToPond(10L);

        assertEquals(10L, equipment.getPondId());
        assertEquals(EquipmentStatus.LINKED, equipment.getStatus());
    }

    @Test
    @DisplayName("Given default constructor, When creating equipment, Then creates empty equipment")
    void createEquipment_DefaultConstructor() {
        Equipment equipment = new Equipment();

        assertNull(equipment.getId());
        assertNull(equipment.getType());
        assertNull(equipment.getName());
    }
}
