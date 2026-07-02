package telemetry_service.telemetry.infrastructure.persistence.jpa.repositories;

import telemetry_service.telemetry.domain.model.entities.SensorPondMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SensorPondMappingRepository extends JpaRepository<SensorPondMapping, Long> {
    Optional<SensorPondMapping> findBySensorId(Long sensorId);
}
