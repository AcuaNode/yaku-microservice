package telemetry_service.telemetry.infrastructure.persistence.jpa.repositories;

import telemetry_service.telemetry.domain.model.aggregates.SpeciesOptimalRange;
import telemetry_service.telemetry.domain.model.valueobjects.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SpeciesOptimalRangeRepository extends JpaRepository<SpeciesOptimalRange, Long> {
    Optional<SpeciesOptimalRange> findByPondIdAndSensorType(Long pondId, SensorType sensorType);
}
