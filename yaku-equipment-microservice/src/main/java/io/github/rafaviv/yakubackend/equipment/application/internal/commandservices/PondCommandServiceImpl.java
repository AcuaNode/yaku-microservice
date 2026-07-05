package io.github.rafaviv.yakubackend.equipment.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.equipment.domain.services.PondCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

import io.github.rafaviv.yakubackend.shared.infrastructure.messaging.mqtt.MqttPublisherConfig.MqttPublisher;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

@Service
public class PondCommandServiceImpl implements PondCommandService {

    private final PondRepository pondRepository;
    private final EquipmentRepository equipmentRepository;
    
    @Autowired(required = false)
    private MqttPublisher mqttPublisher;

    public PondCommandServiceImpl(PondRepository pondRepository, EquipmentRepository equipmentRepository) {
        this.pondRepository = pondRepository;
        this.equipmentRepository = equipmentRepository;
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
            boolean speciesChanged = pond.getSpecies() != species;
            pond.update(name, species, volume);
            Pond savedPond = pondRepository.save(pond);
            
            if (speciesChanged && mqttPublisher != null) {
                List<Equipment> equipments = equipmentRepository.findByPondId(pondId);
                for (Equipment eq : equipments) {
                    String pc = eq.getPhysicalCode();
                    if (pc != null && pc.matches(".*-(B1|B2|TEMP|TURB)$")) {
                        String deviceId = pc.substring(0, pc.lastIndexOf("-"));
                        try {
                            mqttPublisher.publishToMqtt("yaku/config/devices/" + deviceId, species.name());
                        } catch (Exception e) {
                            System.err.println("Failed to publish MQTT routing: " + e.getMessage());
                        }
                    }
                }
            }
            return savedPond;
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
