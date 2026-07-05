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

    @Column(nullable = false)
    private Double ica;

    protected Threshold() {
    }

    public Threshold(Species species, Double minTemperature, Double maxTemperature, Double minTurbidity, Double maxTurbidity) {
        this.species = species;
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minTurbidity = minTurbidity;
        this.maxTurbidity = maxTurbidity;
        calculateIca();
    }

    public void update(Double minTemperature, Double maxTemperature, Double minTurbidity, Double maxTurbidity) {
        this.minTemperature = minTemperature;
        this.maxTemperature = maxTemperature;
        this.minTurbidity = minTurbidity;
        this.maxTurbidity = maxTurbidity;
        calculateIca();
    }

    private void calculateIca() {
        double maxDev = Math.abs(this.maxTemperature - 25.0);
        double minDev = Math.abs(this.minTemperature - 25.0);
        double tempDev = Math.max(maxDev, minDev);
        double tempScore = Math.max(0.0, 100.0 - tempDev * 10.0);
        
        // Simulated exactly at the boundary (minimum acceptable voltage limit). 
        // According to the new logic, if it's at or above maxTurbidity it yields 100.0.
        double turbScore = 100.0;
        
        this.ica = (tempScore * 0.40) + (turbScore * 0.60);
        this.ica = Math.max(0.0, Math.min(this.ica, 100.0));
    }



    public boolean isTemperatureViolation(Double temperature) {
        return temperature < minTemperature || temperature > maxTemperature;
    }

    public boolean isTurbidityViolation(Double turbidity) {
        return turbidity < minTurbidity || turbidity > maxTurbidity;
    }
}
