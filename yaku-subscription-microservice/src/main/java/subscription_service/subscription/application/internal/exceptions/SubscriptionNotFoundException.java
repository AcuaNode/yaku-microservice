package subscription_service.subscription.application.internal.exceptions;

public class SubscriptionNotFoundException extends RuntimeException {
    public SubscriptionNotFoundException(Long userId) {
        super("Subscription with id " + userId + " not found");
    }
}
