package subscription_service.subscription.interfaces.events.services;

import subscription_service.subscription.domain.model.valueobjects.Currency;
import subscription_service.subscription.domain.model.valueobjects.PaymentStatus;

public interface ExternalPaymentService {
    PaymentStatus processPayment(Long userId, Double amount, Currency currency, String paymentMethodId);
}



