package subscription_service.subscription.application.internal.commandservices;

import subscription_service.subscription.application.internal.exceptions.PlanNotFoundException;
import subscription_service.subscription.application.internal.exceptions.SubscriptionNotFoundException;
import subscription_service.subscription.domain.model.aggregates.Subscription;
import subscription_service.subscription.domain.model.entities.Payment;
import subscription_service.subscription.domain.model.entities.Plan;
import subscription_service.subscription.domain.model.valueobjects.Currency;
import subscription_service.subscription.domain.model.valueobjects.PaymentProvider;
import subscription_service.subscription.domain.model.valueobjects.PaymentStatus;
import subscription_service.subscription.domain.model.valueobjects.SubscriptionPeriod;
import subscription_service.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;
import subscription_service.subscription.infrastructure.persistence.jpa.repositories.PaymentRepository;
import subscription_service.subscription.infrastructure.persistence.jpa.repositories.SubscriptionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class SubscriptionCommandServiceImpl implements SubscriptionCommandService {
    private final SubscriptionRepository subscriptionRepository;
    private final PlanRepository planRepository;
    private final PaymentRepository paymentRepository;

    public SubscriptionCommandServiceImpl(SubscriptionRepository subscriptionRepository, PlanRepository planRepository,
            PaymentRepository paymentRepository) {
        this.subscriptionRepository = subscriptionRepository;
        this.planRepository = planRepository;
        this.paymentRepository = paymentRepository;
    }

    @Override
    @Transactional
    public Subscription subscribeUserToPlan(Long userId, Long planId) {
        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Plan not found"));

        SubscriptionPeriod period = new SubscriptionPeriod(
                LocalDate.now(),
                LocalDate.now().plusDays(plan.getDurationInDays()));

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .map(existing -> {
                    existing.setPlan(plan);
                    existing.setPeriod(period);
                    return existing;
                })
                .orElse(new Subscription(userId, plan, period));

        return subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void subscribeUserToPlanWithStripe(Long userId, Long planId, String externalId) {

        Subscription subscription = subscriptionRepository.findByUserId(userId)
                .orElseThrow(() -> new SubscriptionNotFoundException(userId));

        Plan plan = planRepository.findById(planId)
                .orElseThrow(() -> new PlanNotFoundException(planId));

        subscription.subscribeToPlanWithProvider(plan, externalId, PaymentProvider.STRIPE);
        subscriptionRepository.save(subscription);
    }

    @Override
    @Transactional
    public void cancelSubscription(Long userId) {
        subscriptionRepository.findByUserId(userId)
                .ifPresent(subscription -> {
                    subscription.cancel();
                    subscriptionRepository.save(subscription);
                });
    }

    @Override
    @Transactional
    public void processPaymentCallback(Long userId, String transactionReference, boolean success) {
        // Simple implementation to record payment
        Payment payment = new Payment(
                userId,
                0.0, // Should be dynamic
                Currency.USD,
                success ? PaymentStatus.SUCCESS
                        : PaymentStatus.FAILED,
                transactionReference);
        paymentRepository.save(payment);
    }
}
