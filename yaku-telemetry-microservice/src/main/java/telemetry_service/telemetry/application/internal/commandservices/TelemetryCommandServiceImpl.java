package telemetry_service.telemetry.application.internal.commandservices;

import telemetry_service.telemetry.application.outboundservices.acl.ExternalEquipmentService;
import telemetry_service.telemetry.domain.model.aggregates.SensorReading;
import telemetry_service.telemetry.domain.model.commands.GenerateAggregatesCommand;
import telemetry_service.telemetry.domain.model.commands.ProcessGroupedTelemetryCommand;
import telemetry_service.telemetry.domain.model.events.ThresholdBreachedEvent;
import telemetry_service.telemetry.domain.model.valueobjects.MeasurementValue;
import telemetry_service.telemetry.domain.model.valueobjects.SensorType;
import telemetry_service.telemetry.infrastructure.persistence.jpa.repositories.SensorPondMappingRepository;
import telemetry_service.telemetry.infrastructure.persistence.jpa.repositories.SensorReadingRepository;
import telemetry_service.telemetry.infrastructure.persistence.jpa.repositories.ThresholdRepository;
import telemetry_service.telemetry.domain.model.valueobjects.Species;
import telemetry_service.telemetry.interfaces.events.MqttPublisherConfig.MqttPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class TelemetryCommandServiceImpl implements TelemetryCommandService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryCommandServiceImpl.class);

    private final SensorReadingRepository sensorReadingRepository;
    private final ThresholdRepository thresholdRepository;
    private final SensorPondMappingRepository sensorPondMappingRepository;
    private final ExternalEquipmentService externalEquipmentService;
    private final ApplicationEventPublisher eventPublisher;

    @Autowired(required = false)
    private MqttPublisher mqttPublisher;

    public TelemetryCommandServiceImpl(SensorReadingRepository sensorReadingRepository,
                                       ThresholdRepository thresholdRepository,
                                       SensorPondMappingRepository sensorPondMappingRepository,
                                       ExternalEquipmentService externalEquipmentService,
                                       ApplicationEventPublisher eventPublisher) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.thresholdRepository = thresholdRepository;
        this.sensorPondMappingRepository = sensorPondMappingRepository;
        this.externalEquipmentService = externalEquipmentService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public void handle(ProcessGroupedTelemetryCommand command) {
        Long pondId = externalEquipmentService.getPondIdByDeviceId(command.deviceId());

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (command.temperature() != null) {
            sensorReadingRepository.save(new SensorReading(pondId, SensorType.TEMPERATURE, new MeasurementValue(command.temperature(), "C"), now));
        }
        if (command.turbidity() != null) {
            sensorReadingRepository.save(new SensorReading(pondId, SensorType.TURBIDITY, new MeasurementValue(command.turbidity(), "NTU"), now));
        }
        if (command.ica() != null) {
            sensorReadingRepository.save(new SensorReading(pondId, SensorType.ICA, new MeasurementValue(command.ica(), "INDEX"), now));
        }

        try {
            // Resolucion de Identidad
            String speciesName = externalEquipmentService.getSpeciesByPondId(pondId);

            // Carga de Reglas
            thresholdRepository.findBySpecies(Species.valueOf(speciesName)).ifPresentOrElse(threshold -> {
                int anomaliesCount = 0;
                StringBuilder messageBuilder = new StringBuilder();
                messageBuilder.append("Anomalies detected: ");

                // Validacion de Parametros (Lógica Condicional Segura)
                if (command.temperature() != null && threshold.isTemperatureViolation(command.temperature())) {
                    anomaliesCount++;
                    messageBuilder.append(String.format("TEMPERATURE level is %.2f (Allowed: [%.2f, %.2f]). ",
                            command.temperature(), threshold.getMinTemperature(), threshold.getMaxTemperature()));
                }

                if (command.turbidity() != null && threshold.isTurbidityViolation(command.turbidity())) {
                    anomaliesCount++;
                    messageBuilder.append(String.format("TURBIDITY level is %.2f (Allowed: [%.2f, %.2f]). ",
                            command.turbidity(), threshold.getMinTurbidity(), threshold.getMaxTurbidity()));
                }

                // Evaluacion de Escalamiento
                if (anomaliesCount == 0) {
                    return; // Silent finish
                }

                String severity;
                if (anomaliesCount == 1) {
                    severity = "WARNING";
                    Long targetUserId = externalEquipmentService.getOperatorIdByPondId(pondId);
                    messageBuilder.append(String.format("For species %s in pond %d.", speciesName, pondId));
                    ThresholdBreachedEvent event = new ThresholdBreachedEvent(
                            pondId,
                            targetUserId,
                            severity,
                            "[" + severity + "] " + messageBuilder.toString());
                    eventPublisher.publishEvent(event);
                } else {
                    severity = "CRITICAL";
                    Long adminId = externalEquipmentService.getUserIdByPondId(pondId);
                    Long operatorId = externalEquipmentService.getOperatorIdByPondId(pondId);
                    messageBuilder.append(String.format("For species %s in pond %d.", speciesName, pondId));
                    String finalMessage = "[" + severity + "] " + messageBuilder.toString();

                    // Alerta al Admin
                    eventPublisher.publishEvent(new ThresholdBreachedEvent(pondId, adminId, severity, finalMessage));

                    // Alerta al Operador (si es distinto al Admin)
                    if (!adminId.equals(operatorId)) {
                        eventPublisher.publishEvent(new ThresholdBreachedEvent(pondId, operatorId, severity, finalMessage));
                    }
                }
            }, () -> {
                log.warn("No thresholds configured for species: {}", speciesName);
            });

        } catch (Exception e) {
            log.warn("Failed to evaluate telemetry rules for pond {}: {}", pondId, e.getMessage());
        }
    }

    @Override
    @Transactional
    public void handle(GenerateAggregatesCommand command) {
        // Batch processing logic placeholder
    }

    @Override
    @Transactional
    public Long handle(telemetry_service.telemetry.domain.model.commands.ConfigureThresholdCommand command) {
        var speciesEnum = Species.valueOf(command.species().toUpperCase());
        var thresholdOptional = thresholdRepository.findBySpecies(speciesEnum);

        telemetry_service.telemetry.domain.model.aggregates.Threshold threshold;

        if (thresholdOptional.isPresent()) {
            threshold = thresholdOptional.get();
            threshold.update(
                    command.minTemperature(),
                    command.maxTemperature(),
                    command.minTurbidity(),
                    command.maxTurbidity()
            );
            thresholdRepository.save(threshold);
        } else {
            threshold = new telemetry_service.telemetry.domain.model.aggregates.Threshold(
                    speciesEnum,
                    command.minTemperature(),
                    command.maxTemperature(),
                    command.minTurbidity(),
                    command.maxTurbidity()
            );
            thresholdRepository.save(threshold);
        }

        // Publish to MQTT
        if (mqttPublisher != null) {
            try {
                String message = String.format(Locale.US, "{\"maxTemp\": %.2f, \"maxTurb\": %.2f}",
                        threshold.getMaxTemperature(), threshold.getMaxTurbidity());
                mqttPublisher.publishToMqtt("yaku/config/thresholds/" + threshold.getSpecies(), message);
            } catch (Exception e) {
                log.error("Failed to publish thresholds to MQTT: {}", e.getMessage());
            }
        }

        return threshold.getId();
    }

    @Override
    public void executeRemoteCommand(String physicalCode, String command) {
        if (physicalCode == null || physicalCode.isBlank()) {
            throw new IllegalArgumentException("Physical code is required");
        }
        if (command == null || command.isBlank()) {
            throw new IllegalArgumentException("Command is required");
        }

        // physicalCode could be "YAKU-001-B1" or just "YAKU-001"
        // If it's a sub-device code, extract the deviceId and determine the pump
        String deviceId;
        String mqttCommand;

        if (physicalCode.matches(".*-(B1|B2)$")) {
            deviceId = physicalCode.substring(0, physicalCode.lastIndexOf("-"));
            String pumpCode = physicalCode.substring(physicalCode.lastIndexOf("-") + 1);
            mqttCommand = pumpCode.equals("B1") ? "PUMP1_ON" : "PUMP2_ON";
        } else {
            deviceId = physicalCode;
            mqttCommand = command;
        }

        if (mqttPublisher != null) {
            try {
                mqttPublisher.publishToMqtt("yaku/command/devices/" + deviceId, mqttCommand);
                System.out.println("✅ Comando MQTT publicado a: yaku/command/devices/" + deviceId + " -> " + mqttCommand);
            } catch (Exception e) {
                System.err.println("❌ Failed to publish MQTT command: " + e.getMessage());
                throw new RuntimeException("Error publicando comando MQTT", e);
            }
        } else {
            System.err.println("❌ MQTT Publisher is NULL! No se pudo publicar.");
            throw new IllegalStateException("MQTT Publisher no está disponible");
        }
    }
}