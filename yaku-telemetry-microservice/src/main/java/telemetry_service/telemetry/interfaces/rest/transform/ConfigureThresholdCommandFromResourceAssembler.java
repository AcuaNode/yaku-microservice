package telemetry_service.telemetry.interfaces.rest.transform;

import telemetry_service.telemetry.domain.model.commands.ConfigureThresholdCommand;
import telemetry_service.telemetry.interfaces.rest.resources.ConfigureThresholdResource;

public class ConfigureThresholdCommandFromResourceAssembler {
    public static ConfigureThresholdCommand toCommandFromResource(ConfigureThresholdResource resource) {
        return new ConfigureThresholdCommand(
                resource.species(),
                resource.minTemperature(),
                resource.maxTemperature(),
                resource.minTurbidity(),
                resource.maxTurbidity()
        );
    }
}
