package subscription_service.subscription.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import subscription_service.subscription.domain.model.entities.Payment;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
