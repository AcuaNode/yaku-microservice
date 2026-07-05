package subscription_service.subscription.domain.model.aggregates;

import subscription_service.shared.domain.model.entities.AuditableModel;
import subscription_service.subscription.domain.model.entities.Plan;
import subscription_service.subscription.domain.model.valueobjects.PaymentProvider;
import subscription_service.subscription.domain.model.valueobjects.SubscriptionPeriod;
import subscription_service.subscription.domain.model.valueobjects.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "subscriptions")
public class Subscription extends AuditableModel {
    @Column(nullable = false)
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "plan_id", nullable = false)
    private Plan plan;

    @Embedded
    private SubscriptionPeriod period;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    @Column(name = "external_id")
    private String externalId;

    @Enumerated(EnumType.STRING)
    @Column(name = "provider")
    private PaymentProvider provider;

    public Subscription(Long userId, Plan plan, SubscriptionPeriod period) {
        this.userId = userId;
        this.plan = plan;
        this.period = period;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public Subscription(Long userId) {
        this.userId = userId;
        this.status = SubscriptionStatus.CANCELLED;
    }

    /**
     * Lógica de Negocio Encapsulada (Patrón: Tell, Don't Ask)
     * La entidad es responsable de calcular su propio periodo basándose en el plan.
     */
    public void subscribeToPlan(Plan plan) {
        this.plan = plan;
        this.status = SubscriptionStatus.ACTIVE;

        this.period = new SubscriptionPeriod(
                LocalDate.now(),
                LocalDate.now().plusDays(plan.getDurationInDays()));
    }

    public void subscribeToPlanWithProvider(Plan plan, String externalId, PaymentProvider provider) {
        this.subscribeToPlan(plan);
        this.externalId = externalId;
        this.provider = provider;
    }

    public void cancel() {
        this.status = SubscriptionStatus.CANCELLED;
    }

    public void expire() {
        this.status = SubscriptionStatus.EXPIRED;
    }
}
