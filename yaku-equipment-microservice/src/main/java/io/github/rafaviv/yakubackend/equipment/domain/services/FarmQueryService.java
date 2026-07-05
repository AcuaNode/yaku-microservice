package io.github.rafaviv.yakubackend.equipment.domain.services;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetFarmByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetFarmsByOwnerIdQuery;

import java.util.List;
import java.util.Optional;

public interface FarmQueryService {
    List<Farm> handle(GetFarmsByOwnerIdQuery query);
    Optional<Farm> handle(GetFarmByIdQuery query);
}
