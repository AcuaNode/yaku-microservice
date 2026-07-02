package telemetry_service.telemetry.domain.model.aggregates;

import telemetry_service.telemetry.domain.model.valueobjects.MeasurementValue;
import telemetry_service.telemetry.domain.model.valueobjects.SensorType;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "sensor_readings")
@EntityListeners(AuditingEntityListener.class)
@Getter
public class SensorReading extends AbstractAggregateRoot<SensorReading> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pondId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType sensorType;

    @Embedded
    private MeasurementValue measurement;

    @Column(nullable = false, updatable = false)
    private LocalDateTime timestamp;

    protected SensorReading() {
    }

    public SensorReading(Long pondId, SensorType sensorType, MeasurementValue measurement, LocalDateTime timestamp) {
        this.pondId = pondId;
        this.sensorType = sensorType;
        this.measurement = measurement;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
    }
}
