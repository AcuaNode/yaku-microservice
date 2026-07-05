package notification_service.notification.domain.services;

import notification_service.notification.domain.models.aggregates.DeviceToken;
import java.util.List;
import java.util.Optional;

public interface DeviceTokenRepository {
    DeviceToken save(DeviceToken deviceToken);
    Optional<DeviceToken> findByUserIdAndFcmToken(Long userId, String fcmToken);
    List<DeviceToken> findByUserId(Long userId);
}
