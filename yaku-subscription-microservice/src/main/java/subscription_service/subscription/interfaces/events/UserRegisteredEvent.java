package subscription_service.subscription.interfaces.events;

public record UserRegisteredEvent(Long userId, String username, String email, Long farmId) {
}
