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
import org.springframework.transaction.annotation.Transactional;

@Service
public class TelemetryCommandServiceImpl implements TelemetryCommandService {

    private static final Logger log = LoggerFactory.getLogger(TelemetryCommandServiceImpl.class);

    private final SensorReadingRepository sensorReadingRepository;
    private final ThresholdRepository thresholdRepository;
    private final SensorPondMappingRepository sensorPondMappingRepository;
    private final ExternalEquipmentService externalEquipmentService;
    private final ApplicationEventPublisher eventPublisher;

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
        // We will assume the pond is valid. Saving the raw readings for non-null metrics
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (command.temperature() != null) {
            sensorReadingRepository.save(new SensorReading(command.pondId(), SensorType.TEMPERATURE, new MeasurementValue(command.temperature(), "C"), now));
        }
        if (command.ph() != null) {
            sensorReadingRepository.save(new SensorReading(command.pondId(), SensorType.PH, new MeasurementValue(command.ph(), "pH"), now));
        }
        if (command.turbidity() != null) {
            sensorReadingRepository.save(new SensorReading(command.pondId(), SensorType.TURBIDITY, new MeasurementValue(command.turbidity(), "NTU"), now));
        }

        try {
            // Resolucion de Identidad
            String speciesName = externalEquipmentService.getSpeciesByPondId(command.pondId());

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

                if (command.ph() != null && threshold.isPhViolation(command.ph())) {
                    anomaliesCount++;
                    messageBuilder.append(String.format("PH level is %.2f (Allowed: [%.2f, %.2f]). ", 
                            command.ph(), threshold.getMinPh(), threshold.getMaxPh()));
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
                if (anomaliesCount == 1 || anomaliesCount == 2) {
                    severity = "WARNING";
                    Long targetUserId = externalEquipmentService.getOperatorIdByPondId(command.pondId());
                    messageBuilder.append(String.format("For species %s in pond %d.", speciesName, command.pondId()));
                    ThresholdBreachedEvent event = new ThresholdBreachedEvent(
                            command.pondId(),
                            targetUserId,
                            severity,
                            "[" + severity + "] " + messageBuilder.toString()
                    );
                    eventPublisher.publishEvent(event);
                } else {
                    severity = "CRITICAL";
                    Long adminId = externalEquipmentService.getUserIdByPondId(command.pondId());
                    Long operatorId = externalEquipmentService.getOperatorIdByPondId(command.pondId());
                    messageBuilder.append(String.format("For species %s in pond %d.", speciesName, command.pondId()));
                    String finalMessage = "[" + severity + "] " + messageBuilder.toString();
                    
                    // Alerta al Admin
                    eventPublisher.publishEvent(new ThresholdBreachedEvent(command.pondId(), adminId, severity, finalMessage));
                    
                    // Alerta al Operador (si es distinto al Admin)
                    if (!adminId.equals(operatorId)) {
                        eventPublisher.publishEvent(new ThresholdBreachedEvent(command.pondId(), operatorId, severity, finalMessage));
                    }
                }
            }, () -> {
                log.warn("No thresholds configured for species: {}", speciesName);
            });

        } catch (Exception e) {
            log.warn("Failed to evaluate telemetry rules for pond {}: {}", command.pondId(), e.getMessage());
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
        if (thresholdOptional.isPresent()) {
            var threshold = thresholdOptional.get();
            threshold.update(
                    command.minTemperature(),
                    command.maxTemperature(),
                    command.minPh(),
                    command.maxPh(),
                    command.minTurbidity(),
                    command.maxTurbidity()
            );
            thresholdRepository.save(threshold);
            return threshold.getId();
        }

        var threshold = new telemetry_service.telemetry.domain.model.aggregates.Threshold(
                speciesEnum,
                command.minTemperature(),
                command.maxTemperature(),
                command.minPh(),
                command.maxPh(),
                command.minTurbidity(),
                command.maxTurbidity()
        );
        thresholdRepository.save(threshold);
        return threshold.getId();
    }
}