package telemetry_service.telemetry.infrastructure.configuration.infrastructure.adapters.stripe;

import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.Currency;
import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.PaymentStatus;
import io.github.rafaviv.yakubackend.subscription.interfaces.events.services.ExternalPaymentService;
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
