package notification_service.notification.domain.services;


import java.util.List;
import java.util.Optional;
import notification_service.notification.domain.models.aggregates.DeviceToken;

public interface DeviceTokenRepository {
    DeviceToken save(DeviceToken deviceToken);
    Optional<DeviceToken> findByUserIdAndFcmToken(Long userId, String fcmToken);
    List<DeviceToken> findByUserId(Long userId);
}
