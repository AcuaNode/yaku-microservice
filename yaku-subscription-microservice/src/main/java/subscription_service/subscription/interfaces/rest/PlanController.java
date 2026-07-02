package subscription_service.subscription.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import subscription_service.subscription.application.internal.queryservices.SubscriptionQueryService;
import subscription_service.subscription.domain.model.entities.Plan;
import subscription_service.subscription.interfaces.rest.resources.PlanResource;
import subscription_service.subscription.interfaces.rest.transform.PlanResourceFromEntityAssembler;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/plans")
public class PlanController {
    private final SubscriptionQueryService subscriptionQueryService;

    public PlanController(SubscriptionQueryService subscriptionQueryService) {
        this.subscriptionQueryService = subscriptionQueryService;
    }

    @GetMapping
    public ResponseEntity<List<PlanResource>> getAllPlans() {
        List<Plan> plans = subscriptionQueryService.getAvailablePlans();
        List<PlanResource> resources = plans.stream()
                .map(PlanResourceFromEntityAssembler::toResourceFromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(resources);
    }
}
