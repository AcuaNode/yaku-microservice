package subscription_service.subscription.interfaces.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import subscription_service.subscription.application.internal.commandservices.SubscriptionCommandService;
import subscription_service.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;

@Component
public class UserRegisteredEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegisteredEventListener.class);
    private final SubscriptionCommandService subscriptionCommandService;
    private final PlanRepository planRepository;
    private final ObjectMapper objectMapper;

    public UserRegisteredEventListener(SubscriptionCommandService subscriptionCommandService,
                                       PlanRepository planRepository,
                                       ObjectMapper objectMapper) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.planRepository = planRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "user-registered-topic", groupId = "subscription-group")
    public void on(String payload) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(payload, UserRegisteredEvent.class);
            LOGGER.info("Received UserRegisteredEvent from Kafka: {}", event);
            planRepository.findByName("FREE").ifPresent(plan -> {
                subscriptionCommandService.subscribeUserToPlan(event.userId(), plan.getId());
                LOGGER.info("Successfully subscribed user {} to FREE plan", event.userId());
            });
        } catch (Exception e) {
            LOGGER.error("Error processing UserRegisteredEvent from Kafka", e);
        }
    }
}
