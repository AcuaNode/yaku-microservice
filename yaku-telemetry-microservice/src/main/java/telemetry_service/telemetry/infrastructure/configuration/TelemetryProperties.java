package telemetry_service.telemetry.infrastructure.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "telemetry")
@Getter
@Setter
public class TelemetryProperties {

    private Processing processing = new Processing();
    private Storage storage = new Storage();

    @Getter
    @Setter
    public static class Processing {
        private int batchSize = 100;
    }

    @Getter
    @Setter
    public static class Storage {
        private int retentionDays = 30;
    }
}
