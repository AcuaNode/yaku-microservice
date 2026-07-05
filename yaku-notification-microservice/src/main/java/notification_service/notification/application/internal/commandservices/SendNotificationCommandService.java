package notification_service.notification.application.internal.commandservices;

import org.springframework.stereotype.Service;
import notification_service.notification.domain.models.aggregates.Notification;
import notification_service.notification.domain.models.valueobjects.RecipientInfo;
import notification_service.notification.domain.models.valueobjects.TriggerSnapshot;
import notification_service.notification.domain.models.valueobjects.SensorType;
import notification_service.notification.domain.services.NotificationRepository;
import notification_service.notification.domain.services.PushNotificationService;
import notification_service.notification.domain.services.DeviceTokenRepository;
import notification_service.notification.domain.models.aggregates.DeviceToken;
import notification_service.notification.domain.models.commands.SendNotificationCommand;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SendNotificationCommandService {
    private final NotificationRepository notificationRepository;
    private final PushNotificationService pushNotificationService;
    private final DeviceTokenRepository deviceTokenRepository;

    public SendNotificationCommandService(NotificationRepository notificationRepository, 
                                          PushNotificationService pushNotificationService,
                                          DeviceTokenRepository deviceTokenRepository) {
        this.notificationRepository = notificationRepository;
        this.pushNotificationService = pushNotificationService;
        this.deviceTokenRepository = deviceTokenRepository;
    }

    public void handle(SendNotificationCommand command) {
        RecipientInfo recipient = new RecipientInfo(command.userId());
        TriggerSnapshot triggerData = new TriggerSnapshot(command.value(), command.sensorType() != null ? SensorType.valueOf(command.sensorType()) : null, command.hardwareStatus());
        
        Notification notification = new Notification(command.type(), command.message(), recipient, triggerData);
        
        Notification savedNotification = notificationRepository.save(notification);
        
        // Obtener los tokens del usuario
        List<String> tokens = deviceTokenRepository.findByUserId(command.userId())
                .stream()
                .map(DeviceToken::getFcmToken)
                .collect(Collectors.toList());
        
        pushNotificationService.sendNotification(savedNotification, tokens);
    }
}
