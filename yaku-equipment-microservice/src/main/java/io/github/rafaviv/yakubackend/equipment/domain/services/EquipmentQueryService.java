package io.github.rafaviv.yakubackend.equipment.domain.services;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;

import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByFarmIdQuery;

import java.util.List;
import java.util.Optional;

public interface EquipmentQueryService {
    List<Equipment> getAllEquipment();
    Optional<Equipment> handle(GetEquipmentByIdQuery query);
    List<Equipment> handle(GetEquipmentByFarmIdQuery query);
    Optional<Equipment> handle(io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByPhysicalCodeQuery query);
}
