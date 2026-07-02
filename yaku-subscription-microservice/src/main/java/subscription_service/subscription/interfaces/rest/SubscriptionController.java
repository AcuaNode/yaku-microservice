package subscription_service.subscription.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import subscription_service.subscription.application.internal.commandservices.SubscriptionCommandService;
import subscription_service.subscription.application.internal.queryservices.SubscriptionQueryService;
import subscription_service.subscription.domain.model.aggregates.Subscription;
import subscription_service.subscription.interfaces.rest.resources.SubscribeToPlanResource;
import subscription_service.subscription.interfaces.rest.resources.SubscriptionResource;
import subscription_service.subscription.interfaces.rest.transform.SubscriptionResourceFromEntityAssembler;

@RestController
@RequestMapping("/api/v1/subscriptions")
public class SubscriptionController {
    private final SubscriptionCommandService subscriptionCommandService;
    private final SubscriptionQueryService subscriptionQueryService;
    private final subscription_service.subscription.domain.ports.ExternalPaymentGateway externalPaymentGateway;

    public SubscriptionController(SubscriptionCommandService subscriptionCommandService,
            SubscriptionQueryService subscriptionQueryService, subscription_service.subscription.domain.ports.ExternalPaymentGateway externalPaymentGateway) {
        this.subscriptionCommandService = subscriptionCommandService;
        this.subscriptionQueryService = subscriptionQueryService;
        this.externalPaymentGateway = externalPaymentGateway;
    }

    @GetMapping("/{userId}")
    public ResponseEntity<SubscriptionResource> getSubscriptionByUserId(@PathVariable Long userId) {
        return subscriptionQueryService.getUserSubscriptionStatus(userId)
                .map(subscription -> ResponseEntity
                        .ok(SubscriptionResourceFromEntityAssembler.toResourceFromEntity(subscription)))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping("/{userId}")
    public ResponseEntity<SubscriptionResource> subscribeUser(@PathVariable Long userId,
            @RequestBody SubscribeToPlanResource resource) {
        Subscription subscription = subscriptionCommandService.subscribeUserToPlan(userId, resource.planId());
        return ResponseEntity.ok(SubscriptionResourceFromEntityAssembler.toResourceFromEntity(subscription));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> cancelSubscription(@PathVariable Long userId) {
        subscriptionCommandService.cancelSubscription(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{userId}/checkout")
    public ResponseEntity<java.util.Map<String, String>> createCheckoutSession(@PathVariable Long userId,
            @RequestBody SubscribeToPlanResource resource) {
        return subscriptionQueryService.getPlanById(resource.planId())
                .map(plan -> {
                    String checkoutUrl = externalPaymentGateway.createCheckoutSession(userId, plan);
                    java.util.Map<String, String> response = new java.util.HashMap<>();
                    response.put("url", checkoutUrl);
                    return ResponseEntity.ok(response);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
