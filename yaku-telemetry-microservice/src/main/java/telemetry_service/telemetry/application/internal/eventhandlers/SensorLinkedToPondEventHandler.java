package telemetry_service.telemetry.application.internal.eventhandlers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import telemetry_service.telemetry.domain.model.valueobjects.Species;
import telemetry_service.telemetry.infrastructure.persistence.jpa.repositories.ThresholdRepository;
import telemetry_service.telemetry.interfaces.events.MqttPublisherConfig.MqttPublisher;

import java.util.Locale;
import java.util.Map;

@Component
public class SensorLinkedToPondEventHandler {

    private final ThresholdRepository thresholdRepository;
    private final ObjectMapper objectMapper;

    @Autowired(required = false)
    private MqttPublisher mqttPublisher;

    public SensorLinkedToPondEventHandler(ThresholdRepository thresholdRepository, ObjectMapper objectMapper) {
        this.thresholdRepository = thresholdRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "yaku.equipment.sensor-linked", groupId = "telemetry-group")
    public void onSensorLinkedToPond(String eventJson) {
        try {
            Map<String, Object> event = objectMapper.readValue(eventJson, Map.class);
            String species = (String) event.get("species");
            Long pondId = Long.valueOf(event.get("pondId").toString());

            if (species == null || species.isBlank()) {
                System.out.println("⚠️ [KAFKA] Evento sin especie, ignorando configuración MQTT.");
                return;
            }

            System.out.println("📥 [KAFKA] Sensor enlazado al pond " + pondId + " con especie: " + species);

            // Buscar los thresholds para esta especie y publicar la configuración vía MQTT
            thresholdRepository.findBySpecies(Species.valueOf(species)).ifPresent(threshold -> {
                if (mqttPublisher != null) {
                    try {
                        String message = String.format(Locale.US, "{\"maxTemp\": %.2f, \"maxTurb\": %.2f}",
                                threshold.getMaxTemperature(), threshold.getMaxTurbidity());
                        // We need the deviceId from the sensor's physicalCode; 
                        // the event carries sensorId which we can't easily resolve here.
                        // Instead, publish to the species-specific topic for the Edge Processor.
                        mqttPublisher.publishToMqtt("yaku/config/thresholds/" + species, message);
                        System.out.println("📤 [MQTT] Config publicada para especie " + species + ": " + message);
                    } catch (Exception e) {
                        System.err.println("❌ Failed to publish MQTT config: " + e.getMessage());
                    }
                }
            });
        } catch (Exception e) {
            System.err.println("❌ Error procesando evento Kafka SensorLinkedToPond: " + e.getMessage());
        }
    }
}