package subscription_service.subscription.interfaces.rest.resources;

public record SubscriptionResource(Long id, Long userId, Long planId, String planName, String status, String startDate,
                String endDate) {
}
