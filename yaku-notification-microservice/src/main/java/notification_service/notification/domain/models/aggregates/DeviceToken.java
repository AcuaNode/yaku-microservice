package notification_service.notification.domain.models.aggregates;

import java.time.LocalDateTime;

public class DeviceToken {
    private Long id;
    private final Long userId;
    private final String fcmToken;
    private final LocalDateTime registeredAt;

    public DeviceToken(Long userId, String fcmToken) {
        if (userId == null) throw new IllegalArgumentException("UserId cannot be null");
        if (fcmToken == null || fcmToken.isBlank()) throw new IllegalArgumentException("Token cannot be blank");
        this.userId = userId;
        this.fcmToken = fcmToken;
        this.registeredAt = LocalDateTime.now();
    }

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public String getFcmToken() { return fcmToken; }
    public LocalDateTime getRegisteredAt() { return registeredAt; }
}
