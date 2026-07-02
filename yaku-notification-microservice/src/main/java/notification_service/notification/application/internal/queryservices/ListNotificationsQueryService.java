package notification_service.notification.application.internal.queryservices;

import org.springframework.stereotype.Service;

import notification_service.notification.domain.models.aggregates.Notification;
import notification_service.notification.domain.models.queries.ListNotificationsQuery;
import notification_service.notification.domain.services.NotificationRepository;

import java.util.List;

@Service
public class ListNotificationsQueryService {
    private final NotificationRepository notificationRepository;

    public ListNotificationsQueryService(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    public List<Notification> handle(ListNotificationsQuery query) {
        return notificationRepository.findByRecipientUserId(query.userId());
    }
}
