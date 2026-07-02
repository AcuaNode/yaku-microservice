package subscription_service.subscription.interfaces.events;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import subscription_service.subscription.application.internal.commandservices.SubscriptionCommandService;
import subscription_service.subscription.domain.model.events.UserRegisteredEvent;
import subscription_service.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;

import org.springframework.kafka.annotation.KafkaListener;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class UserRegisteredEventListener {
    private final SubscriptionCommandService subscriptionCommandService;
    private final PlanRepository planRepository;
    private final ObjectMapper objectMapper;

    public UserRegisteredEventListener(SubscriptionCommandService subscriptionCommandService,
            PlanRepository planRepository, ObjectMapper objectMapper) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.planRepository = planRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "iam.user-registered", groupId = "subscription-group")
    public void on(String eventJson) {
        try {
            UserRegisteredEvent event = objectMapper.readValue(eventJson, UserRegisteredEvent.class);
            planRepository.findByName("FREE").ifPresent(plan -> {
                subscriptionCommandService.subscribeUserToPlan(event.userId(), plan.getId());
            });
        } catch (Exception e) {
            System.err.println("Failed to process UserRegisteredEvent: " + e.getMessage());
        }
    }
}
