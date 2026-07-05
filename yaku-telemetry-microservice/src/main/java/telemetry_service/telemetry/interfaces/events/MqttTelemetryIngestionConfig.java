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
import org.springframework.integration.mqtt.core.MqttPahoClientFactory;
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

    @Bean
    public MessageProducer inbound(MqttPahoClientFactory mqttClientFactory) {
        MqttPahoMessageDrivenChannelAdapter adapter =
                new MqttPahoMessageDrivenChannelAdapter("yaku-telemetry-subscriber", mqttClientFactory, "yaku/telemetria/pond/+");
        adapter.setCompletionTimeout(5000);
        adapter.setConverter(new DefaultPahoMessageConverter());
        adapter.setQos(1);
        adapter.setOutputChannel(mqttInputChannel());
        return adapter;
    }

    @Bean
    @ServiceActivator(inputChannel = "mqttInputChannel")
    public MessageHandler handler() {
        return new MessageHandler() {
            @Override
            public void handleMessage(Message<?> message) {
                try {
                    String payload = (String) message.getPayload();
                    // The Arduino sends: {deviceId, temperatura, turbidez, ica, estado}
                    // We map temperatura->temperature, turbidez->turbidity for the command
                    var jsonNode = objectMapper.readTree(payload);
                    ProcessGroupedTelemetryCommand command = new ProcessGroupedTelemetryCommand(
                            jsonNode.has("deviceId") ? jsonNode.get("deviceId").asText() : null,
                            jsonNode.has("temperatura") ? jsonNode.get("temperatura").asDouble() : (jsonNode.has("temperature") ? jsonNode.get("temperature").asDouble() : null),
                            jsonNode.has("turbidez") ? jsonNode.get("turbidez").asDouble() : (jsonNode.has("turbidity") ? jsonNode.get("turbidity").asDouble() : null),
                            jsonNode.has("ica") ? jsonNode.get("ica").asDouble() : null
                    );
                    telemetryCommandService.handle(command);
                    System.out.println("📥 [MQTT] Telemetría procesada para dispositivo: " + command.deviceId());
                } catch (Exception e) {
                    System.err.println("Error processing MQTT message: " + e.getMessage());
                }
            }
        };
    }
}