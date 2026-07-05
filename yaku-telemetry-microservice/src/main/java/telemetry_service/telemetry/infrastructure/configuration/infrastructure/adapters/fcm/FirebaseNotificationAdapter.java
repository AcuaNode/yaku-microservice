package telemetry_service.telemetry.infrastructure.configuration.infrastructure.adapters.fcm;

import io.github.rafaviv.yakubackend.subscription.interfaces.events.services.NotificationService;
import org.springframework.stereotype.Service;

@Service
public class FirebaseNotificationAdapter implements NotificationService {
    @Override
    public void sendNotification(Long userId, String message) {
        // Placeholder for Firebase implementation
        System.out.println("Sending FCM notification to user " + userId + ": " + message);
    }
}
