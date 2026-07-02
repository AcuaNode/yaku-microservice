package subscription_service.subscription.interfaces.rest.resources;

public record PlanResource(Long id, String name, Double price, String currency, Integer maxPonds,
                Integer durationInDays) {
}
