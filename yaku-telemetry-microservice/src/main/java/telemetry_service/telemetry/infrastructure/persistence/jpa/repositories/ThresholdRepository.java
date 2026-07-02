package telemetry_service.telemetry.infrastructure.persistence.jpa.repositories;

import telemetry_service.telemetry.domain.model.aggregates.Threshold;
import telemetry_service.telemetry.domain.model.valueobjects.Species;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ThresholdRepository extends JpaRepository<Threshold, Long> {
    Optional<Threshold> findBySpecies(Species species);
}