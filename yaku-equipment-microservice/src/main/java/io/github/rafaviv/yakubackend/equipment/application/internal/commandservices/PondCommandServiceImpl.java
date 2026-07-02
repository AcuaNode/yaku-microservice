package io.github.rafaviv.yakubackend.equipment.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PondCommandServiceImpl implements PondCommandService {

    private final PondRepository pondRepository;

    public PondCommandServiceImpl(PondRepository pondRepository) {
        this.pondRepository = pondRepository;
    }

    @Override
    public Optional<Pond> createPond(Long farmId, String name, Species species, Double volume) {
        Pond pond = new Pond(farmId, name, species, volume);
        return Optional.of(pondRepository.save(pond));
    }

    @Override
    public void deletePond(Long pondId) {
        if (!pondRepository.existsById(pondId)) {
            throw new IllegalArgumentException("Pond not found with id: " + pondId);
        }
        pondRepository.deleteById(pondId);
    }

    @Override
    public Optional<Pond> updatePond(Long pondId, String name, Species species, Double volume) {
        return pondRepository.findById(pondId).map(pond -> {
            pond.update(name, species, volume);
            return pondRepository.save(pond);
        });
    }

    @Override
    public Optional<Pond> assignOperator(Long pondId, Long operatorId) {
        return pondRepository.findById(pondId).map(pond -> {
            pond.assignOperator(operatorId);
            return pondRepository.save(pond);
        });
    }

    @Override
    public Optional<Pond> deassignOperator(Long pondId) {
        return pondRepository.findById(pondId).map(pond -> {
            pond.deassignOperator();
            return pondRepository.save(pond);
        });
    }
}