package io.github.rafaviv.yakubackend.equipment.application.internal.commandservices;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Equipment;
import io.github.rafaviv.yakubackend.equipment.domain.model.events.EquipmentRegistrationRequested;
import io.github.rafaviv.yakubackend.equipment.domain.model.events.SensorLinkedToPondEvent;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.equipment.domain.services.EquipmentCommandService;
import io.github.rafaviv.yakubackend.equipment.infrastructure.events.SpringDomainEventPublisher;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.EquipmentRepository;
import io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories.PondRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import io.github.rafaviv.yakubackend.shared.infrastructure.messaging.mqtt.MqttPublisherConfig.MqttPublisher;
import org.springframework.beans.factory.annotation.Autowired;

@Service
public class EquipmentCommandServiceImpl implements EquipmentCommandService {

    private final EquipmentRepository equipmentRepository;
    private final PondRepository pondRepository;
    private final SpringDomainEventPublisher eventPublisher;

    @Autowired(required = false)
    private MqttPublisher mqttPublisher;

    public EquipmentCommandServiceImpl(EquipmentRepository equipmentRepository, PondRepository pondRepository,
            SpringDomainEventPublisher eventPublisher) {
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
    public List<Equipment> registerIoTDevice(String deviceId, String deviceName, Long farmId) {
        List<Equipment> createdEquipments = new ArrayList<>();

        // 1. Bomba 1
        createdEquipments.add(
                registerEquipment(EquipmentType.ACTUATOR, deviceName + " - Bomba 1", deviceId + "-B1", farmId).get());
        // 2. Bomba 2
        createdEquipments.add(
                registerEquipment(EquipmentType.ACTUATOR, deviceName + " - Bomba 2", deviceId + "-B2", farmId).get());
        // 3. Sensor de Temperatura
        createdEquipments
                .add(registerEquipment(EquipmentType.SENSOR, deviceName + " - Sensor Temp", deviceId + "-TEMP", farmId)
                        .get());
        // 4. Sensor de Turbidez
        createdEquipments
                .add(registerEquipment(EquipmentType.SENSOR, deviceName + " - Sensor Turb", deviceId + "-TURB", farmId)
                        .get());

        return createdEquipments;
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

        // Check if it's an IoT device piece and link the rest automatically
        String physicalCode = savedEquipment.getPhysicalCode();
        if (physicalCode != null && physicalCode.matches(".*-(B1|B2|TEMP|TURB)$")) {
            String deviceId = physicalCode.substring(0, physicalCode.lastIndexOf("-"));
            List<Equipment> allEquipments = equipmentRepository.findAll();
            for (Equipment eq : allEquipments) {
                if (eq.getPhysicalCode() != null && eq.getPhysicalCode().startsWith(deviceId + "-")
                        && !eq.getId().equals(savedEquipment.getId())) {
                    if (eq.getPondId() == null || !eq.getPondId().equals(pond.getId())) {
                        eq.linkToPond(pond.getId());
                        equipmentRepository.save(eq);
                        if (eq.getType() == EquipmentType.SENSOR) {
                            eventPublisher.publish(new SensorLinkedToPondEvent(eq.getId(), pond.getId()));
                        }
                    }
                }
            }
            // Publicar el routing a MQTT
            if (mqttPublisher != null) {
                try {
                    String speciesStr = pond.getSpecies().name();
                    mqttPublisher.publishToMqtt("yaku/config/devices/" + deviceId, speciesStr);
                } catch (Exception e) {
                    System.err.println("Failed to publish MQTT routing: " + e.getMessage());
                }
            }
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

    @Override
    public void executeRemoteCommand(Long equipmentId) {
        var equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));

        if (equipment.getType() != EquipmentType.ACTUATOR) {
            throw new IllegalArgumentException("Equipment is not an actuator");
        }

        String physicalCode = equipment.getPhysicalCode();
        if (physicalCode == null || !physicalCode.matches(".*-(B1|B2)$")) {
            throw new IllegalArgumentException("Equipment physical code does not support remote activation");
        }

        String deviceId = physicalCode.substring(0, physicalCode.lastIndexOf("-"));
        String pumpCode = physicalCode.substring(physicalCode.lastIndexOf("-") + 1);
        String command = pumpCode.equals("B1") ? "PUMP1_ON" : "PUMP2_ON";

        if (mqttPublisher != null) {
            try {
                mqttPublisher.publishToMqtt("yaku/command/devices/" + deviceId, command);
                System.out.println("✅ Comando MQTT publicado a: yaku/command/devices/" + deviceId + " -> " + command);
            } catch (Exception e) {
                System.err.println("❌ Failed to publish MQTT command: " + e.getMessage());
            }
        } else {
            System.err.println("❌ MQTT Publisher is NULL! No se pudo publicar.");
            throw new IllegalArgumentException("Error interno: MQTT Publisher no está disponible en el backend");
        }
    }
}
