package subscription_service.subscription.infrastructure.adapters.stripe;

import subscription_service.subscription.domain.model.valueobjects.Currency;
import subscription_service.subscription.domain.model.valueobjects.PaymentStatus;
import subscription_service.subscription.interfaces.events.services.ExternalPaymentService;
import org.springframework.stereotype.Service;

@Service
public class StripePaymentAdapter implements ExternalPaymentService {
    @Override
    public PaymentStatus processPayment(Long userId, Double amount, Currency currency, String paymentMethodId) {
        // Placeholder for Stripe implementation
        System.out
                .println("Processing payment of " + amount + " " + currency + " for user " + userId + " using Stripe");
        return PaymentStatus.SUCCESS;
    }
}
