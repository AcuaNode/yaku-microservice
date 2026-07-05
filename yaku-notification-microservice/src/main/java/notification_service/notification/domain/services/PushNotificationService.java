package notification_service.notification.domain.services;

import notification_service.notification.domain.models.aggregates.Notification;
import java.util.List;

public interface PushNotificationService {
    void sendNotification(Notification notification, List<String> fcmTokens);
}
