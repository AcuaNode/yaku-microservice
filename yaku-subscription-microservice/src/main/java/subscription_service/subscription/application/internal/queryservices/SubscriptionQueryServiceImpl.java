package subscription_service.subscription.application.internal.queryservices;

import subscription_service.subscription.domain.model.aggregates.Subscription;
import subscription_service.subscription.domain.model.entities.Plan;
import subscription_service.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;
import subscription_service.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class SubscriptionQueryServiceImpl implements SubscriptionQueryService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;

    public SubscriptionQueryServiceImpl(SubscriptionRepository subscriptionRepository, PlanRepository planRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
    }


    @Override
    public Optional<Subscription> getUserSubscriptionStatus(Long userId) {
        return subscriptionRepository.findByUserId(userId);
    }

    @Override
    public List<Plan> getAvailablePlans() {
        return planRepository.findAll();
    }

    @Override
    public Optional<Plan> getPlanById(Long planId) {
        return planRepository.findById(planId);
    }
}
