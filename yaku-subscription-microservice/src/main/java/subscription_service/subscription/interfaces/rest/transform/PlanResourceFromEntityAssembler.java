package subscription_service.subscription.interfaces.rest.transform;

import subscription_service.subscription.domain.model.entities.Plan;
import subscription_service.subscription.interfaces.rest.resources.PlanResource;

public class PlanResourceFromEntityAssembler {
    public static PlanResource toResourceFromEntity(Plan entity) {
        return new PlanResource(
                entity.getId(),
                entity.getName(),
                entity.getPrice(),
                entity.getCurrency().name(),
                entity.getMaxPonds(),
                entity.getDurationInDays());
    }
}
