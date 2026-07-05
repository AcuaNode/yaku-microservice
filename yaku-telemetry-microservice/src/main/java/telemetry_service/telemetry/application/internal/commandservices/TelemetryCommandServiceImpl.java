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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.Locale;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import telemetry_service.shared.infrastructure.messaging.mqtt.MqttPublisherConfig.MqttPublisher;

@Service
public class TelemetryCommandServiceImpl implements TelemetryCommandService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryCommandServiceImpl.class);

    private final SensorReadingRepository sensorReadingRepository;
    private final ThresholdRepository thresholdRepository;
    private final SensorPondMappingRepository sensorPondMappingRepository;
    private final ExternalEquipmentService externalEquipmentService;
    private final ApplicationEventPublisher eventPublisher;
    private final io.github.rafaviv.yakubackend.equipment.interfaces.acl.EquipmentContextFacade equipmentContextFacade;

    @Autowired(required = false)
    private MqttPublisher mqttPublisher;

    public TelemetryCommandServiceImpl(SensorReadingRepository sensorReadingRepository,
            ThresholdRepository thresholdRepository,
            SensorPondMappingRepository sensorPondMappingRepository,
            ExternalEquipmentService externalEquipmentService,
            ApplicationEventPublisher eventPublisher,
            io.github.rafaviv.yakubackend.equipment.interfaces.acl.EquipmentContextFacade equipmentContextFacade) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.thresholdRepository = thresholdRepository;
        this.sensorPondMappingRepository = sensorPondMappingRepository;
        this.externalEquipmentService = externalEquipmentService;
        this.eventPublisher = eventPublisher;
        this.equipmentContextFacade = equipmentContextFacade;
    }

    @Override
    @Transactional
    public void handle(ProcessGroupedTelemetryCommand command) {
        // Resolvemos el pondId dinámicamente usando el deviceId
        Long pondId = equipmentContextFacade.getPondIdByDeviceId(command.deviceId());

        // We will assume the pond is valid. Saving the raw readings for non-null
        // metrics
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (command.temperature() != null) {
            sensorReadingRepository.save(new SensorReading(pondId, SensorType.TEMPERATURE,
                    new MeasurementValue(command.temperature(), "C"), now));
        }
        if (command.turbidity() != null) {
            sensorReadingRepository.save(new SensorReading(pondId, SensorType.TURBIDITY,
                    new MeasurementValue(command.turbidity(), "NTU"), now));
        }
        if (command.ica() != null) {
            sensorReadingRepository.save(new SensorReading(pondId, SensorType.ICA,
                    new MeasurementValue(command.ica(), "INDEX"), now));
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
                        eventPublisher
                                .publishEvent(new ThresholdBreachedEvent(pondId, operatorId, severity, finalMessage));
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
    public Long handle(
            telemetry_service.telemetry.domain.model.commands.ConfigureThresholdCommand command) {
        var speciesEnum = Species.valueOf(command.species());
        var thresholdOptional = thresholdRepository.findBySpecies(speciesEnum);

        telemetry_service.telemetry.domain.model.aggregates.Threshold threshold;

        if (thresholdOptional.isPresent()) {
            threshold = thresholdOptional.get();
            threshold.update(
                    command.minTemperature(),
                    command.maxTemperature(),
                    command.minTurbidity(),
                    command.maxTurbidity());
            thresholdRepository.save(threshold);
        } else {
            threshold = new telemetry_service.telemetry.domain.model.aggregates.Threshold(
                    speciesEnum,
                    command.minTemperature(),
                    command.maxTemperature(),
                    command.minTurbidity(),
                    command.maxTurbidity());
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
}
