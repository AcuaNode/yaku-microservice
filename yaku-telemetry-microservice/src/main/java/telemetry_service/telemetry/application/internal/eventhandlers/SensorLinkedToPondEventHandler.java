package telemetry_service.telemetry.application.internal.eventhandlers;

import io.github.rafaviv.yakubackend.equipment.domain.model.events.SensorLinkedToPondEvent;
import telemetry_service.telemetry.domain.model.entities.SensorPondMapping;
import telemetry_service.telemetry.infrastructure.persistence.jpa.repositories.SensorPondMappingRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

@Service
public class SensorLinkedToPondEventHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(SensorLinkedToPondEventHandler.class);

    private final SensorPondMappingRepository repository;

    public SensorLinkedToPondEventHandler(SensorPondMappingRepository repository) {
        this.repository = repository;
    }

    @EventListener
    public void on(SensorLinkedToPondEvent event) {
        LOGGER.info("Received SensorLinkedToPondEvent for sensor {} and pond {}", event.sensorId(), event.pondId());
        
        SensorPondMapping mapping = new SensorPondMapping(event.sensorId(), event.pondId());
        repository.save(mapping);
        
        LOGGER.info("Saved SensorPondMapping for sensor {}", event.sensorId());
    }
}
