package subscription_service.subscription.interfaces.events.services;

public interface NotificationService {
    void sendNotification(Long userId, String message);
}
