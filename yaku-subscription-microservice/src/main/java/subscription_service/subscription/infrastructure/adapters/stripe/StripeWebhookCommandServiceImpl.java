package subscription_service.subscription.infrastructure.adapters.stripe;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import subscription_service.subscription.application.internal.commandservices.SubscriptionCommandService;

@Service
public class StripeWebhookCommandServiceImpl {

    private final SubscriptionCommandService subscriptionCommandService;

    public StripeWebhookCommandServiceImpl(SubscriptionCommandService subscriptionCommandService) {
        this.subscriptionCommandService = subscriptionCommandService;

    }

    @Transactional
    public void handleCheckoutSessionCompleted(String userIdStr, String planIdStr, String stripeSubscriptionId) {
        if (userIdStr != null && planIdStr != null) {
            Long userId = Long.valueOf(userIdStr);
            Long planId = Long.valueOf(planIdStr);

            subscriptionCommandService.subscribeUserToPlanWithStripe(userId, planId, stripeSubscriptionId);

            System.out.println("Checkout session completed and subscription activated for user " + userId);
        } else {
            System.err.println("Missing userId or planId in session completed event");
        }
    }
}
