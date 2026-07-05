package subscription_service.subscription.domain.ports;

public interface ExternalPaymentGateway {
    String generatePaymentIntent(Long amountInCents, String currency);
    String createCheckoutSession(Long userId, subscription_service.subscription.domain.model.entities.Plan plan);
}
