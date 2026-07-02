package notification_service.notification.application.internal.commandservices;

import org.springframework.stereotype.Service;

import notification_service.notification.domain.models.aggregates.DeviceToken;
import notification_service.notification.domain.models.commands.RegisterDeviceTokenCommand;
import notification_service.notification.domain.services.DeviceTokenRepository;


@Service
public class RegisterDeviceTokenCommandService {
    private final DeviceTokenRepository deviceTokenRepository;

    public RegisterDeviceTokenCommandService(DeviceTokenRepository deviceTokenRepository) {
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public void handle(RegisterDeviceTokenCommand command) {
        var existingToken = deviceTokenRepository.findByUserIdAndFcmToken(command.userId(), command.fcmToken());
        if (existingToken.isEmpty()) {
            DeviceToken newToken = new DeviceToken(command.userId(), command.fcmToken());
            deviceTokenRepository.save(newToken);
        }
    }
}
