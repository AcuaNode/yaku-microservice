package io.github.rafaviv.yakubackend.equipment.application.internal.queryservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentQueryService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import org.springframework.stereotype.Service;

import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByIdQuery;
import io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByFarmIdQuery;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentQueryServiceImpl implements EquipmentQueryService {

    private final EquipmentRepository equipmentRepository;

    public EquipmentQueryServiceImpl(EquipmentRepository equipmentRepository) {
        this.equipmentRepository = equipmentRepository;
    }

    @Override
    public List<Equipment> getAllEquipment() {
        return equipmentRepository.findAll();
    }

    @Override
    public Optional<Equipment> handle(GetEquipmentByIdQuery query) {
        return equipmentRepository.findById(query.id());
    }

    @Override
    public List<Equipment> handle(GetEquipmentByFarmIdQuery query) {
        return equipmentRepository.findByFarmId(query.farmId());
    }

    @Override
    public Optional<Equipment> handle(io.github.rafaviv.yakubackend.equipment.domain.model.queries.GetEquipmentByPhysicalCodeQuery query) {
        return equipmentRepository.findByPhysicalCode(query.physicalCode());
    }
}
