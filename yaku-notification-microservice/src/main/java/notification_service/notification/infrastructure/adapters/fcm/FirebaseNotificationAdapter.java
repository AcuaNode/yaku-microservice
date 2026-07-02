package notification_service.notification.infrastructure.adapters.fcm;

import notification_service.notification.domain.models.aggregates.Notification;
import notification_service.notification.domain.services.PushNotificationService;
import org.springframework.stereotype.Service;

import java.util.List;

public class FirebaseNotificationAdapter implements PushNotificationService {
    @Override
    public void sendNotification(Notification notification, List<String> fcmTokens) {
        // Placeholder for Firebase implementation
        System.out.println("Sending FCM notification: " + notification.getMessage() + " to " + fcmTokens.size() + " devices.");
    }
}
