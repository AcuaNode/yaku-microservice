package notification_service.notification.domain.models.commands;

public record RegisterDeviceTokenCommand(Long userId, String fcmToken) {}
