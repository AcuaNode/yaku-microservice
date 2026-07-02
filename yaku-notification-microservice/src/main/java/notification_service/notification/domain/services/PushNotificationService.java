package notification_service.notification.domain.services;

import java.util.List;

import notification_service.notification.domain.models.aggregates.Notification;

public interface PushNotificationService {
    void sendNotification(Notification notification, List<String> fcmTokens);
}
