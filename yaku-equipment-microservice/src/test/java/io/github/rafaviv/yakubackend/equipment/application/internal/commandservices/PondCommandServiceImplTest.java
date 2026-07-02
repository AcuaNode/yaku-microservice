package io.github.rafaviv.yakubackend.equipment.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
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
class PondCommandServiceImplTest {

    @Mock
    private PondRepository pondRepository;

    @Mock
    private FarmRepository farmRepository;

    private PondCommandService pondCommandService;

    @BeforeEach
    void setUp() {
        pondCommandService = new PondCommandServiceImpl(pondRepository);
    }

    @Test
    @DisplayName("Given farm belongs to owner, When createPond, Then creates pond successfully")
    void createPond_OwnerMatches_Creates() {
        Farm farm = new Farm("Test Farm", 1L, "Address");
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));
        when(pondRepository.save(any(Pond.class))).thenAnswer(inv -> {
            Pond p = inv.getArgument(0);
            return p;
        });

        Optional<Pond> result = pondCommandService.createPond(10L, "New Pond", Species.TILAPIA, 100.0);

        assertTrue(result.isPresent());
        assertEquals("New Pond", result.get().getName());
        assertEquals("Tilapia", result.get().getSpecies());
        assertEquals(100.0, result.get().getVolume());
    }

    @Test
    @DisplayName("Given farm does not exist, When createPond, Then throws IllegalArgumentException")
    void createPond_FarmNotFound_Throws() {
        when(farmRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pondCommandService.createPond(99L, "New Pond",Species.TILAPIA, 100.0));
        assertEquals("Farm not found with id: 99", ex.getMessage());
        verify(pondRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given farm belongs to different owner, When createPond, Then throws IllegalArgumentException")
    void createPond_OwnerMismatch_Throws() {
        Farm farm = new Farm("Test Farm", 2L, "Address");
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pondCommandService.createPond(10L, "New Pond", Species.TILAPIA, 100.0));
        assertEquals("Farm not found with id: 10", ex.getMessage());
        verify(pondRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given pond exists and farm belongs to owner, When deletePond, Then deletes successfully")
    void deletePond_OwnerMatches_Deletes() {
        Farm farm = new Farm("Test Farm", 1L, "Address");
        Pond pond = new Pond(10L, "Test Pond", Species.TILAPIA, 100.0);
        when(pondRepository.findById(5L)).thenReturn(Optional.of(pond));
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));
        doNothing().when(pondRepository).deleteById(5L);

        assertDoesNotThrow(() -> pondCommandService.deletePond(5L));
        verify(pondRepository).deleteById(5L);
    }

    @Test
    @DisplayName("Given pond does not exist, When deletePond, Then throws IllegalArgumentException")
    void deletePond_PondNotFound_Throws() {
        when(pondRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pondCommandService.deletePond(99L));
        assertEquals("Pond not found with id: 99", ex.getMessage());
        verify(pondRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Given pond's farm belongs to different owner, When deletePond, Then throws IllegalArgumentException")
    void deletePond_OwnerMismatch_Throws() {
        Farm farm = new Farm("Test Farm", 2L, "Address");
        Pond pond = new Pond(10L, "Test Pond", Species.TILAPIA, 100.0);
        when(pondRepository.findById(5L)).thenReturn(Optional.of(pond));
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pondCommandService.deletePond(5L));
        assertEquals("Pond not found with id: 5", ex.getMessage());
        verify(pondRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("Given pond exists and farm belongs to owner, When updatePond, Then updates successfully")
    void updatePond_OwnerMatches_Updates() {
        Farm farm = new Farm("Test Farm", 1L, "Address");
        Pond pond = new Pond(10L, "Old Name", Species.PAICHE, 50.0);
        when(pondRepository.findById(5L)).thenReturn(Optional.of(pond));
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));
        when(pondRepository.save(any(Pond.class))).thenAnswer(inv -> inv.getArgument(0));

        Optional<Pond> result = pondCommandService.updatePond(5L, "New Name", Species.TRUCHA, 75.0);

        assertTrue(result.isPresent());
        assertEquals("New Name", result.get().getName());
        assertEquals("New Species", result.get().getSpecies());
        assertEquals(75.0, result.get().getVolume());
    }

    @Test
    @DisplayName("Given pond's farm belongs to different owner, When updatePond, Then throws IllegalArgumentException")
    void updatePond_OwnerMismatch_Throws() {
        Farm farm = new Farm("Test Farm", 2L, "Address");
        Pond pond = new Pond(10L, "Old Name", Species.PAICHE, 50.0);
        when(pondRepository.findById(5L)).thenReturn(Optional.of(pond));
        when(farmRepository.findById(10L)).thenReturn(Optional.of(farm));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pondCommandService.updatePond(5L, "New Name", Species.TRUCHA, 75.0));
        assertEquals("Pond not found with id: 5", ex.getMessage());
        verify(pondRepository, never()).save(any());
    }

    @Test
    @DisplayName("Given pond does not exist, When updatePond, Then throws IllegalArgumentException")
    void updatePond_PondNotFound_Throws() {
        when(pondRepository.findById(99L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pondCommandService.updatePond(99L, "New Name", Species.TRUCHA, 75.0));
        assertEquals("Pond not found with id: 99", ex.getMessage());
    }

    @Test
    @DisplayName("Given pond exists but farm not found, When updatePond, Then throws IllegalArgumentException")
    void updatePond_FarmNotFound_Throws() {
        Pond pond = new Pond();
        when(pondRepository.findById(5L)).thenReturn(Optional.of(pond));
        when(farmRepository.findById(10L)).thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> pondCommandService.updatePond(5L, "New Name", Species.TRUCHA, 75.0));
        assertEquals("Farm not found with id: 10", ex.getMessage());
    }
}