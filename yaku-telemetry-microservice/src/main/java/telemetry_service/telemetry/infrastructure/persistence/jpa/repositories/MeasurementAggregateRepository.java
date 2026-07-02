package telemetry_service.telemetry.infrastructure.persistence.jpa.repositories;

import telemetry_service.telemetry.domain.model.aggregates.MeasurementAggregate;
import telemetry_service.telemetry.domain.model.valueobjects.SensorType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeasurementAggregateRepository extends JpaRepository<MeasurementAggregate, Long> {
    
    List<MeasurementAggregate> findByPondIdAndPeriodStartGreaterThanEqual(Long pondId, LocalDateTime start);
    
    List<MeasurementAggregate> findByPondIdAndSensorTypeAndPeriodStartGreaterThanEqual(Long pondId, SensorType sensorType, LocalDateTime start);
}
