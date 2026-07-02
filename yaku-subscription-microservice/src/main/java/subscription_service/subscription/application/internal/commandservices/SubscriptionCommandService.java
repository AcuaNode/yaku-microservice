package subscription_service.subscription.application.internal.commandservices;

import subscription_service.subscription.domain.model.aggregates.Subscription;

public interface SubscriptionCommandService {
    Subscription subscribeUserToPlan(Long userId, Long planId);

    void subscribeUserToPlanWithStripe(Long userId, Long planId, String externalId);
    void cancelSubscription(Long userId);

    void processPaymentCallback(Long userId, String transactionReference, boolean success);
}
