package subscription_service.subscription.application.internal.exceptions;

public class PlanNotFoundException extends RuntimeException {
    public PlanNotFoundException(Long planId) {
        super("Plan with id " + planId + " not found");
    }
}
