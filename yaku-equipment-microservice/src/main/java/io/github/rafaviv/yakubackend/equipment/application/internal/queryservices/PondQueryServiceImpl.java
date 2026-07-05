package io.github.rafaviv.yakubackend.equipment.application.internal.queryservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetAllPondsQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondsByFarmIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondsByAssignedOperatorIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondQueryService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class PondQueryServiceImpl implements PondQueryService {

    private final PondRepository pondRepository;

    public PondQueryServiceImpl(PondRepository pondRepository) {
        this.pondRepository = pondRepository;
    }

    @Override
    public Optional<Pond> handle(GetPondByIdQuery query) {
        return pondRepository.findById(query.id());
    }

    @Override
    public List<Pond> handle(GetAllPondsQuery query) {
        return pondRepository.findAll();
    }

    @Override
    public List<Pond> handle(GetPondsByFarmIdQuery query) {
        return pondRepository.findByFarmId(query.farmId());
    }

    @Override
    public List<Pond> handle(GetPondsByAssignedOperatorIdQuery query) {
        return pondRepository.findByAssignedOperatorId(query.operatorId());
    }
}
