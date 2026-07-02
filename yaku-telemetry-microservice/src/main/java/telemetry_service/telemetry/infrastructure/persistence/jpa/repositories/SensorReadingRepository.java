package telemetry_service.telemetry.infrastructure.persistence.jpa.repositories;

import telemetry_service.telemetry.domain.model.aggregates.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
    
    List<SensorReading> findByPondIdAndTimestampBetween(Long pondId, LocalDateTime start, LocalDateTime end);
    List<SensorReading> findByPondIdOrderByTimestampDesc(Long pondId);
    void deleteByTimestampBefore(LocalDateTime date);
}
