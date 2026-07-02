package io.github.rafaviv.yakubackend.equipment.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.events.EquipmentRegistrationRequested;
import io.github.rafaviv.yakubackend.equipment.domain.model.events.SensorLinkedToPondEvent;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.events.SpringDomainEventPublisher;
import io.github.rafaviv.yakubackend.equipment.infrastructure.events.kafka.KafkaDomainEventPublisher;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EquipmentCommandServiceImpl implements EquipmentCommandService {

    private final EquipmentRepository equipmentRepository;
    private final PondRepository pondRepository;
    private final SpringDomainEventPublisher eventPublisher;

    public EquipmentCommandServiceImpl(EquipmentRepository equipmentRepository, PondRepository pondRepository, SpringDomainEventPublisher eventPublisher) {
        this.equipmentRepository = equipmentRepository;
        this.pondRepository = pondRepository;
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Optional<Equipment> registerEquipment(EquipmentType type, String name, String physicalCode, Long farmId) {
        Equipment equipment = new Equipment(type, name, physicalCode, farmId);
        Equipment savedEquipment = equipmentRepository.save(equipment);
        
        try {
            eventPublisher.publish(new EquipmentRegistrationRequested(savedEquipment.getId()));
        } catch (Exception e) {
            // Se asume que si el evento lanza una excepción es porque el plan no lo permite
            throw new RuntimeException("El plan no permite más agregaciones", e);
        }
        
        return Optional.of(savedEquipment);
    }

    @Override
    public Optional<Equipment> linkEquipmentToPond(Long equipmentId, Long pondId) {
        var equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));
        var pond = pondRepository.findById(pondId)
                .orElseThrow(() -> new IllegalArgumentException("Pond not found"));
                
        equipment.linkToPond(pond.getId());
        Equipment savedEquipment = equipmentRepository.save(equipment);
        
        if (savedEquipment.getType() == EquipmentType.SENSOR) {
            eventPublisher.publish(new SensorLinkedToPondEvent(savedEquipment.getId(), pond.getId()));
        }
        
        return Optional.of(savedEquipment);
    }

    @Override
    public void deleteEquipment(Long equipmentId) {
        if (!equipmentRepository.existsById(equipmentId)) {
            throw new IllegalArgumentException("Equipment not found with id: " + equipmentId);
        }
        equipmentRepository.deleteById(equipmentId);
    }
}