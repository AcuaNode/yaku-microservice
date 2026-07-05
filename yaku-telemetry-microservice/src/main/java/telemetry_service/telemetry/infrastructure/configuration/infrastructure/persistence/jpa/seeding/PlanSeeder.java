package telemetry_service.telemetry.infrastructure.configuration.infrastructure.persistence.jpa.seeding;

import io.github.rafaviv.yakubackend.subscription.domain.model.entities.Plan;
import io.github.rafaviv.yakubackend.subscription.domain.model.valueobjects.Currency;
import io.github.rafaviv.yakubackend.subscription.infrastructure.persistence.jpa.repositories.PlanRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class PlanSeeder implements CommandLineRunner {
    private final PlanRepository planRepository;

    public PlanSeeder(PlanRepository planRepository) {
        this.planRepository = planRepository;
    }

    @Override
    public void run(String... args) {
        if (planRepository.count() == 0) {
            planRepository.save(new Plan("FREE", 0.0, Currency.USD, 1, 3650, null));
            planRepository.save(new Plan("PREMIUM", 19.99, Currency.USD, 10, 30, null));
            System.out.println("Default plans seeded.");
        }
    }
}
