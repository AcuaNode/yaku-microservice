package telemetry_service.telemetry.infrastructure.configuration.infrastructure.persistence.jpa.seeding;

import telemetry_service.telemetry.domain.model.aggregates.Threshold;
import telemetry_service.telemetry.domain.model.valueobjects.Species;
import telemetry_service.telemetry.infrastructure.persistence.jpa.repositories.ThresholdRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class ThresholdSeeder implements CommandLineRunner {
    private final ThresholdRepository thresholdRepository;

    public ThresholdSeeder(ThresholdRepository thresholdRepository) {
        this.thresholdRepository = thresholdRepository;
    }

    @Override
    public void run(String... args) {
        if (!thresholdRepository.existsBySpecies(Species.TRUCHA)) {
            thresholdRepository.save(new Threshold(Species.TRUCHA, 10.0, 18.0, 3.5, 4.5));
            System.out.println("Seeded TRUCHA thresholds.");
        }
        if (!thresholdRepository.existsBySpecies(Species.PAICHE)) {
            thresholdRepository.save(new Threshold(Species.PAICHE, 25.0, 32.0, 2.5, 3.8));
            System.out.println("Seeded PAICHE thresholds.");
        }
        if (!thresholdRepository.existsBySpecies(Species.TILAPIA)) {
            thresholdRepository.save(new Threshold(Species.TILAPIA, 20.0, 30.0, 3.0, 4.0));
            System.out.println("Seeded TILAPIA thresholds.");
        }
    }
}
