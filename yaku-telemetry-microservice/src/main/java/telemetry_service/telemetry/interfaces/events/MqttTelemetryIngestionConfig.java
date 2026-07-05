package telemetry_service.telemetry.interfaces.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import telemetry_service.telemetry.application.internal.commandservices.TelemetryCommandService;
import telemetry_service.telemetry.domain.model.commands.ProcessGroupedTelemetryCommand;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.integration.annotation.ServiceActivator;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.mqtt.inbound.MqttPahoMessageDrivenChannelAdapter;
import org.springframework.integration.mqtt.support.DefaultPahoMessageConverter;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageHandler;

@Configuration
public class MqttTelemetryIngestionConfig {

    private final TelemetryCommandService telemetryCommandService;
    private final ObjectMapper objectMapper;

    public MqttTelemetryIngestionConfig(TelemetryCommandService telemetryCommandService, ObjectMapper objectMapper) {
        this.telemetryCommandService = telemetryCommandService;
        this.objectMapper = objectMapper;
    }

    @Bean
    public MessageChannel mqttInputChannel() {
        return new DirectChannel();
    }

    // This Bean is commented out to avoid connection errors if the MQTT Broker is
    // not running.
    // Uncomment and configure with the actual broker URL and topics.
    /*
     * @Bean
     * public MessageProducer inbound() {
     * MqttPahoMessageDrivenChannelAdapter adapter =
     * new MqttPahoMessageDrivenChannelAdapter("tcp://localhost:1883",
     * "yaku-backend-client", "telemetry/ponds/#");
     * adapter.setCompletionTimeout(5000);
     * adapter.setConverter(new DefaultPahoMessageConverter());
     * adapter.setQos(1);
     * adapter.setOutputChannel(mqttInputChannel());
     * return adapter;
     * }
     */

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) {
                try {
                    String payload = (String) message.getPayload();
                    // Assuming the payload is a JSON matching ProcessGroupedTelemetryCommand
                    ProcessGroupedTelemetryCommand command = objectMapper.readValue(payload,
                            ProcessGroupedTelemetryCommand.class);
                    telemetryCommandService.handle(command);
                } catch (Exception e) {
                    System.err.println("Error processing MQTT message: " + e.getMessage());
                }
            }
        };
    }
}
