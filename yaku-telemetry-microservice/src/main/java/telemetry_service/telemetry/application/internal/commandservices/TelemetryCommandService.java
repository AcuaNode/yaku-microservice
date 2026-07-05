package telemetry_service.telemetry.application.internal.commandservices;

import telemetry_service.telemetry.domain.model.commands.GenerateAggregatesCommand;
import telemetry_service.telemetry.domain.model.commands.ProcessGroupedTelemetryCommand;

public interface TelemetryCommandService {
    void handle(ProcessGroupedTelemetryCommand command);
    void handle(GenerateAggregatesCommand command);
    Long handle(telemetry_service.telemetry.domain.model.commands.ConfigureThresholdCommand command);
    void executeRemoteCommand(String physicalCode, String command);
}