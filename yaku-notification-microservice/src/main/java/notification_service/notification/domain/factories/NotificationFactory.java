package notification_service.notification.domain.factories;

import notification_service.notification.domain.models.aggregates.Notification;
import notification_service.notification.domain.models.valueobjects.NotificationType;
import notification_service.notification.domain.models.valueobjects.RecipientInfo;
import notification_service.notification.domain.models.valueobjects.SensorType;
import notification_service.notification.domain.models.valueobjects.TriggerSnapshot;

import java.math.BigDecimal;

/**
 * Pattern: Factory (Creational)
 * Centraliza la lógica de creación de notificaciones para evitar que la capa
 * de aplicación tenga que conocer los detalles de instanciación de los Value Objects internos.
 */
public class NotificationFactory {

    public static Notification createCriticalTelemetryAlert(
            Long userId, String message, Double currentValue, SensorType sensorType) {

        RecipientInfo recipient = new RecipientInfo(userId);
        TriggerSnapshot snapshot = new TriggerSnapshot(
                BigDecimal.valueOf(currentValue),
                sensorType,
                "CRITICAL_STATE"
        );

        return new Notification(NotificationType.CRITICAL, message, recipient, snapshot);
    }
}
