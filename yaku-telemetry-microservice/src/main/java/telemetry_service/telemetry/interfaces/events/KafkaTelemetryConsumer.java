package telemetry_service.telemetry.interfaces.events;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import telemetry_service.telemetry.application.internal.commandservices.TelemetryCommandService;
import telemetry_service.telemetry.domain.model.commands.ProcessGroupedTelemetryCommand;

@Component
public class KafkaTelemetryConsumer {

    private final TelemetryCommandService telemetryCommandService;

    public KafkaTelemetryConsumer(TelemetryCommandService telemetryCommandService) {
        this.telemetryCommandService = telemetryCommandService;
    }

    @KafkaListener(topics = "yaku.telemetry.ponds", groupId = "telemetry-group")
    public void consumeTelemetryEvent(ProcessGroupedTelemetryCommand command) {
        try {
            // El mismo servicio que ya probaste y validaste con Postman
            telemetryCommandService.handle(command); 
        } catch (Exception e) {
            System.err.println("Error procesando evento de Kafka: " + e.getMessage());
        }
    }
}