package subscription_service.subscription.infrastructure.persistence.jpa.repositories;

import subscription_service.subscription.domain.model.entities.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
