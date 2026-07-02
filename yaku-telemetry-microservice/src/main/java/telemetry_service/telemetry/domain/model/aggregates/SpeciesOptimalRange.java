package telemetry_service.telemetry.domain.model.aggregates;

import telemetry_service.telemetry.domain.model.valueobjects.SensorType;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

@Entity
@Table(name = "species_optimal_ranges")
@Getter
public class SpeciesOptimalRange extends AbstractAggregateRoot<SpeciesOptimalRange> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long pondId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SensorType sensorType;

    @Column(nullable = false)
    private Double minAllowed;

    @Column(nullable = false)
    private Double maxAllowed;

    protected SpeciesOptimalRange() {
    }

    public SpeciesOptimalRange(Long pondId, SensorType sensorType, Double minAllowed, Double maxAllowed) {
        this.pondId = pondId;
        this.sensorType = sensorType;
        this.minAllowed = minAllowed;
        this.maxAllowed = maxAllowed;
    }

    public boolean isViolation(Double value) {
        return value < minAllowed || value > maxAllowed;
    }
}
