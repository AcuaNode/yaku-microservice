package notification_service.notification.domain.services;


import java.util.List;
import java.util.Optional;

import notification_service.notification.domain.models.aggregates.Notification;

public interface NotificationRepository {
    Notification save(Notification notification);
    Optional<Notification> findById(Long id);
    List<Notification> findByRecipientUserId(Long userId);
}
