package notification_service.notification.infrastructure.persistance.jpa;

import org.springframework.stereotype.Repository;
import notification_service.notification.domain.models.aggregates.DeviceToken;
import notification_service.notification.domain.services.DeviceTokenRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class DeviceTokenRepositoryAdapter implements DeviceTokenRepository {

    private final DeviceTokenJpaRepository jpaRepository;

    public DeviceTokenRepositoryAdapter(DeviceTokenJpaRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public DeviceToken save(DeviceToken deviceToken) {
        DeviceTokenEntity entity = new DeviceTokenEntity();
        if (deviceToken.getId() != null) {
            entity.setId(deviceToken.getId());
        }
        entity.setUserId(deviceToken.getUserId());
        entity.setFcmToken(deviceToken.getFcmToken());
        entity.setRegisteredAt(deviceToken.getRegisteredAt());
        
        DeviceTokenEntity savedEntity = jpaRepository.save(entity);
        deviceToken.setId(savedEntity.getId());
        return deviceToken;
    }

    @Override
    public Optional<DeviceToken> findByUserIdAndFcmToken(Long userId, String fcmToken) {
        return jpaRepository.findByUserIdAndFcmToken(userId, fcmToken).map(this::toDomainModel);
    }

    @Override
    public List<DeviceToken> findByUserId(Long userId) {
        return jpaRepository.findByUserId(userId).stream()
                .map(this::toDomainModel)
                .collect(Collectors.toList());
    }

    private DeviceToken toDomainModel(DeviceTokenEntity entity) {
        DeviceToken deviceToken = new DeviceToken(entity.getUserId(), entity.getFcmToken());
        deviceToken.setId(entity.getId());
        return deviceToken;
    }
}
