package io.github.rafaviv.yakubackend.equipment.domain.services;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetAllPondsQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondsByFarmIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetPondsByAssignedOperatorIdQuery;

import java.util.List;
import java.util.Optional;

public interface PondQueryService {
    Optional<Pond> handle(GetPondByIdQuery query);
    List<Pond> handle(GetAllPondsQuery query);
    List<Pond> handle(GetPondsByFarmIdQuery query);
    List<Pond> handle(GetPondsByAssignedOperatorIdQuery query);
}
