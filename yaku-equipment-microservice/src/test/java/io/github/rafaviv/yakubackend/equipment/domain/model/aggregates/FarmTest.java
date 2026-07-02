package io.github.rafaviv.yakubackend.equipment.domain.model.aggregates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class FarmTest {

    @Test
    @DisplayName("Given valid farm data, When creating farm, Then it is created with generated token")
    void createFarm_Successfully() {
        String name = "My Fish Farm";
        Long ownerId = 1L;
        String address = "123 Farm Street";

        Farm farm = new Farm(name, ownerId, address);

        assertEquals(name, farm.getName());
        assertEquals(ownerId, farm.getOwnerId());
        assertEquals(address, farm.getAddress());
    }

    @Test
    @DisplayName("Given default constructor, When creating farm, Then creates empty farm")
    void createFarm_DefaultConstructor() {
        Farm farm = new Farm();

        assertNull(farm.getId());
        assertNull(farm.getName());
        assertNull(farm.getOwnerId());
    }
}
