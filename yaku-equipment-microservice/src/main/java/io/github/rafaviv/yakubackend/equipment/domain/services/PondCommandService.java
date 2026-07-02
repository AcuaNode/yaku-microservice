package io.github.rafaviv.yakubackend.equipment.domain.services;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;

import java.util.Optional;

public interface PondCommandService {
    Optional<Pond> createPond(Long farmId, String name, Species species, Double volume);
    void deletePond(Long pondId);
    Optional<Pond> updatePond(Long pondId, String name, Species species, Double volume);
    Optional<Pond> assignOperator(Long pondId, Long operatorId);
    Optional<Pond> deassignOperator(Long pondId);
}
