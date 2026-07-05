package com.yaku.gateway.iam.infrastructure.brokers.kafka.producer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yaku.gateway.iam.domain.model.events.UserRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserRegisteredEventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegisteredEventProducer.class);
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public UserRegisteredEventProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper objectMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        try {
            String payload = objectMapper.writeValueAsString(event);
            LOGGER.info("Publishing UserRegisteredEvent to Kafka topic 'user-registered-topic': {}", payload);
            kafkaTemplate.send("user-registered-topic", String.valueOf(event.userId()), payload);
        } catch (JsonProcessingException e) {
            LOGGER.error("Failed to serialize UserRegisteredEvent for Kafka", e);
        }
    }
}
