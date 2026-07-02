package notification_service.notification.infrastructure.persistance.jpa;

import org.springframework.stereotype.Repository;

import notification_service.notification.domain.models.aggregates.Notification;
import notification_service.notification.domain.models.valueobjects.RecipientInfo;
import notification_service.notification.domain.models.valueobjects.TriggerSnapshot;
import notification_service.notification.domain.services.NotificationRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        NotificationEntity entity = new NotificationEntity();
        if (notification.getId() != null) {
            entity.setId(notification.getId());
        }
        entity.setType(notification.getType());
        entity.setMessage(notification.getMessage());
        entity.setRecipientUserId(notification.getRecipient().userId());
        entity.setTriggerValue(notification.getTriggerData().value());
        entity.setTriggerSensorType(notification.getTriggerData().sensorType());
        entity.setTriggerHardwareStatus(notification.getTriggerData().hardwareStatus());
        entity.setCreatedAt(notification.getCreatedAt());

        NotificationEntity savedEntity = jpaRepository.save(entity);
        notification.setId(savedEntity.getId());
        return notification;
    }

    @Override
    public Optional<Notification> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomainModel);
    }

    @Override
    public List<Notification> findByRecipientUserId(Long userId) {
        return jpaRepository.findByRecipientUserIdOrderByCreatedAtDesc(userId).stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    private Notification toDomainModel(NotificationEntity entity) {
        RecipientInfo recipient = new RecipientInfo(entity.getRecipientUserId());
        TriggerSnapshot trigger = new TriggerSnapshot(entity.getTriggerValue(), entity.getTriggerSensorType(), entity.getTriggerHardwareStatus());
        Notification notification = new Notification(entity.getType(), entity.getMessage(), recipient, trigger);
        notification.setId(entity.getId());
        return notification;
    }
}