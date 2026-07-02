package telemetry_service.telemetry.domain.model.entities;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "sensor_pond_mappings")
public class SensorPondMapping {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long sensorId;

    @Column(nullable = false)
    private Long pondId;

    protected SensorPondMapping() {
    }

    public SensorPondMapping(Long sensorId, Long pondId) {
        this.sensorId = sensorId;
        this.pondId = pondId;
    }
}
