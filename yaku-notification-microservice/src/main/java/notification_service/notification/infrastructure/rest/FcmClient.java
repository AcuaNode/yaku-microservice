package notification_service.notification.infrastructure.rest;

import org.springframework.stereotype.Service;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.MulticastMessage;

import notification_service.notification.domain.models.aggregates.Notification;
import notification_service.notification.domain.services.PushNotificationService;
import java.util.List;

@Service
public class FcmClient implements PushNotificationService {

    @Override
    public void sendNotification(Notification notification, List<String> fcmTokens) {
        if (fcmTokens == null || fcmTokens.isEmpty()) {
            System.out.println("No se puede enviar push: El usuario " + notification.getRecipient().userId() + " no tiene tokens registrados.");
            return;
        }

        try {
            // Construimos un cuerpo de notificación detallado con los datos de telemetría si existen
            StringBuilder bodyBuilder = new StringBuilder(notification.getMessage());
            if (notification.getTriggerData() != null) {
                var trigger = notification.getTriggerData();
                bodyBuilder.append("\n");
                if (trigger.value() != null && trigger.sensorType() != null) {
                    bodyBuilder.append(trigger.sensorType().name()).append(": ").append(trigger.value()).append(" | ");
                }
                if (trigger.hardwareStatus() != null) {
                    bodyBuilder.append("Hardware: ").append(trigger.hardwareStatus());
                }
            }
            String finalBody = bodyBuilder.toString().trim();
            if (finalBody.endsWith("|")) {
                finalBody = finalBody.substring(0, finalBody.length() - 2).trim();
            }

            // Construimos la notificación push multicast de Firebase
            var messageBuilder = MulticastMessage.builder()
                    .addAllTokens(fcmTokens)
                    .setNotification(com.google.firebase.messaging.Notification.builder()
                            .setTitle("Alerta de Poza: " + notification.getType())
                            .setBody(finalBody)
                            .build())
                    .putData("notificationId", String.valueOf(notification.getId()))
                    .putData("type", notification.getType().name());

            // También pasamos los valores como datos estructurados (data payload)
            if (notification.getTriggerData() != null) {
                var trigger = notification.getTriggerData();
                if (trigger.value() != null) {
                    messageBuilder.putData("value", String.valueOf(trigger.value()));
                }
                if (trigger.sensorType() != null) {
                    messageBuilder.putData("sensorType", trigger.sensorType().name());
                }
                if (trigger.hardwareStatus() != null) {
                    messageBuilder.putData("hardwareStatus", trigger.hardwareStatus());
                }
            }

            MulticastMessage message = messageBuilder
                    .setAndroidConfig(AndroidConfig.builder()
                            .setPriority(AndroidConfig.Priority.HIGH)
                            .build())
                    .build();

            // Enviamos de forma asíncrona a todos los dispositivos registrados
            BatchResponse response = FirebaseMessaging.getInstance().sendEachForMulticast(message);
            System.out.println("Enviadas exitosamente: " + response.getSuccessCount() + " notificaciones. Fallidas: " + response.getFailureCount());
            
            // Diagnóstico detallado de fallos
            if (response.getFailureCount() > 0) {
                var responsesList = response.getResponses();
                for (int i = 0; i < responsesList.size(); i++) {
                    var sendResponse = responsesList.get(i);
                    if (!sendResponse.isSuccessful()) {
                        System.err.println("Token fallido [" + i + "]: " + fcmTokens.get(i));
                        System.err.println("Causa del fallo: " + sendResponse.getException().getMessage());
                        if (sendResponse.getException() != null) {
                            sendResponse.getException().printStackTrace();
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Fallo al enviar notificación multicast a Firebase: " + e.getMessage());
        }
    }
}