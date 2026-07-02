package subscription_service.subscription.interfaces.rest.transform;

import subscription_service.subscription.domain.model.aggregates.Subscription;
import subscription_service.subscription.interfaces.rest.resources.SubscriptionResource;

public class SubscriptionResourceFromEntityAssembler {
    public static SubscriptionResource toResourceFromEntity(Subscription entity) {
        return new SubscriptionResource(
                entity.getId(),
                entity.getUserId(),
                entity.getPlan().getId(),
                entity.getPlan().getName(),
                entity.getStatus().name(),
                entity.getPeriod().startDate().toString(),
                entity.getPeriod().endDate().toString());
    }
}
