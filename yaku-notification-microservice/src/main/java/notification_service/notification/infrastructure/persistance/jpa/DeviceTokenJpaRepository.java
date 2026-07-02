package notification_service.notification.infrastructure.persistance.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeviceTokenJpaRepository extends JpaRepository<DeviceTokenEntity, Long> {
    Optional<DeviceTokenEntity> findByUserIdAndFcmToken(Long userId, String fcmToken);
    List<DeviceTokenEntity> findByUserId(Long userId);
}
