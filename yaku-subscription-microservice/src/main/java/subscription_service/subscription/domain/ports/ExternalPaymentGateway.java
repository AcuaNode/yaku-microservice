package subscription_service.subscription.domain.ports;

import subscription_service.subscription.domain.model.entities.Plan;

public interface ExternalPaymentGateway {
    String generatePaymentIntent(Long amountInCents, String currency);
    String createCheckoutSession(Long userId, Plan plan);
}
