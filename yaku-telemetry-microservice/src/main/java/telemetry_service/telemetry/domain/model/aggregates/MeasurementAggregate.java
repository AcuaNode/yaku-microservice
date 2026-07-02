package telemetry_service.telemetry.domain.model.aggregates;

import telemetry_service.telemetry.domain.model.valueobjects.SensorType;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

import java.time.LocalDateTime;

@Entity
@Table(name = "measurement_aggregates")
@Getter
public class MeasurementAggregate extends AbstractAggregateRoot<MeasurementAggregate> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pondId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType sensorType;

    private Double minValue;
    private Double maxValue;
    private Double averageValue;

    @Column(nullable = false)
    private LocalDateTime periodStart;

    @Column(nullable = false)
    private LocalDateTime periodEnd;

    protected MeasurementAggregate() {
    }

    public MeasurementAggregate(Long pondId, SensorType sensorType, Double minValue, Double maxValue, Double averageValue, LocalDateTime periodStart, LocalDateTime periodEnd) {
        this.pondId = pondId;
        this.sensorType = sensorType;
        this.minValue = minValue;
        this.maxValue = maxValue;
        this.averageValue = averageValue;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
    }
}
