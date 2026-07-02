package io.github.rafaviv.yakubackend.equipment.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.domain.model.commands.CreateFarmCommand;
import io.github.rafaviv.yakubackend.equipment.domain.services.FarmCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.FarmRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FarmCommandServiceImpl implements FarmCommandService {

    private final FarmRepository farmRepository;

    public FarmCommandServiceImpl(FarmRepository farmRepository) {
        this.farmRepository = farmRepository;
    }

    @Override
    public Optional<Farm> handle(CreateFarmCommand command) {
        Farm farm = new Farm(command.name(), command.ownerId(), command.address());
        Farm savedFarm = farmRepository.save(farm);
        return Optional.of(savedFarm);
    }

    @Override
    public void deleteFarm(Long farmId) {
        if (!farmRepository.existsById(farmId)) {
            throw new IllegalArgumentException("Farm not found with id: " + farmId);
        }
        farmRepository.deleteById(farmId);
    }

    @Override
    public Optional<Farm> regenerateToken(Long farmId) {
        return farmRepository.findById(farmId).map(farm -> {
            farm.regenerateFarmToken();
            return farmRepository.save(farm);
        });
    }
}