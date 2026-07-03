package telemetry_service.telemetry.domain.model.aggregates;

import telemetry_service.telemetry.domain.model.valueobjects.Species;
import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.domain.AbstractAggregateRoot;

@Entity
@Table(name = "thresholds")
@Getter
public class Threshold extends AbstractAggregateRoot<Threshold> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, unique = true)
    private Species species;

    @Column(nullable = false)
    private Double minTemperature;

    @Column(nullable = false)
    private Double maxTemperature;



    @Column(nullable = false)
    private Double minTurbidity;

    @Column(nullable = false)
    private Double maxTurbidity;

    protected Threshold() {
    }

    public Threshold(Species species, Double minTemperature, Double maxTemperature, Double minTurbidity, Double maxTurbidity) {
        this.species = species;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minTurbidity = minTurbidity;
        this.maxTurbidity = maxTurbidity;
    }

    public boolean isTemperatureViolation(Double temperature) {
        return temperature < minTemperature || temperature > maxTemperature;
    }

    public boolean isTurbidityViolation(Double turbidity) {
        return turbidity < minTurbidity || turbidity > maxTurbidity;
    }

    public void update(Double minTemperature, Double maxTemperature, Double minTurbidity, Double maxTurbidity) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minTurbidity = minTurbidity;
        this.maxTurbidity = maxTurbidity;
    }
}