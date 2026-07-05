package subscription_service.subscription.domain.model.entities;

import subscription_service.shared.domain.model.entities.AuditableModel;
import subscription_service.subscription.domain.model.valueobjects.Currency;
import subscription_service.subscription.domain.model.valueobjects.PaymentStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@Table(name = "payments")
public class Payment extends AuditableModel {

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Double amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Currency currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status;

    private String transactionReference;

    public Payment(Long userId, Double amount, Currency currency, PaymentStatus status, String transactionReference) {
        this.userId = userId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
        this.transactionReference = transactionReference;
    }
}
