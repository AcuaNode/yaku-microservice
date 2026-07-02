package io.github.rafaviv.yakubackend.equipment.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.domain.model.commands.CreateFarmCommand;
import io.github.rafaviv.yakubackend.equipment.domain.services.FarmCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FarmCommandServiceImplTest {

    @Mock
    private FarmRepository farmRepository;

    private FarmCommandService farmCommandService;

    @BeforeEach
    void setUp() {
        farmCommandService = new FarmCommandServiceImpl(farmRepository);
    }

    @Test
    @DisplayName("Given farm belongs to owner, When deleteFarm, Then deletes successfully")
    void deleteFarm_OwnerMatches_Deletes() {
        Farm farm = new Farm("Test Farm", 1L, "Address");
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));
        doNothing().when(farmRepository).deleteById(10L);

        assertDoesNotThrow(() -> farmCommandService.deleteFarm(10L));
        verify(farmRepository).deleteById(10L);
    }

    @Test
    @DisplayName("Given farm does not exist, When deleteFarm, Then throws IllegalArgumentException")
    void deleteFarm_FarmNotFound_Throws() {
        when(farmRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> farmCommandService.deleteFarm(99L));
        assertEquals("Farm not found with id: 99", ex.getMessage());
        verify(farmRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Given farm belongs to different owner, When deleteFarm, Then throws IllegalArgumentException")
    void deleteFarm_OwnerMismatch_Throws() {
        Farm farm = new Farm("Test Farm", 2L, "Address");
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> farmCommandService.deleteFarm(10L));
        assertEquals("Farm not found with id: 10", ex.getMessage());
        verify(farmRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Given farm belongs to owner, When regenerateToken, Then returns updated farm")
    void regenerateToken_OwnerMatches_ReturnsUpdatedFarm() {
        Farm farm = new Farm("Test Farm", 1L, "Address");
        String originalToken = farm.getFarmToken();
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));
        when(farmRepository.save(any(Farm.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Farm> result = farmCommandService.regenerateToken(10L);

        assertTrue(result.isPresent());
        assertNotEquals(originalToken, result.get().getFarmToken());
    }

    @Test
    @DisplayName("Given farm does not exist, When regenerateToken, Then throws IllegalArgumentException")
    void regenerateToken_FarmNotFound_Throws() {
        when(farmRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> farmCommandService.regenerateToken(99L));
        assertEquals("Farm not found with id: 99", ex.getMessage());
        verify(farmRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given farm belongs to different owner, When regenerateToken, Then throws IllegalArgumentException")
    void regenerateToken_OwnerMismatch_Throws() {
        Farm farm = new Farm("Test Farm", 2L, "Address");
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> farmCommandService.regenerateToken(10L));
        assertEquals("Farm not found with id: 10", ex.getMessage());
        verify(farmRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given valid CreateFarmCommand, When handle, Then creates and returns farm")
    void handle_ValidCommand_CreatesFarm() {
        CreateFarmCommand command = new CreateFarmCommand("New Farm", 1L, "New Address");
        Farm savedFarm = new Farm("New Farm", 1L, "New Address");
        when(farmRepository.save(any(Farm.class))).thenReturn(savedFarm);

        Optional<Farm> result = farmCommandService.handle(command);

        assertTrue(result.isPresent());
        assertEquals("New Farm", result.get().getName());
        assertEquals(1L, result.get().getOwnerId());
    }
}