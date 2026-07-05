package io.github.rafaviv.yakubackend.equipment.domain.services;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.domain.model.commands.CreateFarmCommand;

import java.util.Optional;

public interface FarmCommandService {
    Optional<Farm> handle(CreateFarmCommand command);
    void deleteFarm(Long farmId);
    Optional<Farm> regenerateToken(Long farmId);
}
