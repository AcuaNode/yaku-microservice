package subscription_service.subscription.domain.model.events;

public record UserRegisteredEvent(Long userId, String username, String email, String farmToken) {
}
