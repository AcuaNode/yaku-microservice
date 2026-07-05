package notification_service.notification.application.internal.eventhandlers;

import notification_service.notification.application.internal.commandservices.SendNotificationCommandService;
import notification_service.notification.domain.models.commands.SendNotificationCommand;
import notification_service.notification.domain.models.valueobjects.NotificationType;
import io.github.rafaviv.yakubackend.telemetry.domain.model.events.ThresholdBreachedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class ThresholdBreachedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(ThresholdBreachedEventHandler.class);
    private final SendNotificationCommandService commandService;

    public ThresholdBreachedEventHandler(SendNotificationCommandService commandService) {
        this.commandService = commandService;
    }

    @EventListener
    public void on(ThresholdBreachedEvent event) {
        log.info("Received ThresholdBreachedEvent for pond {} targetUser {}", event.pondId(), event.targetUserId());

        NotificationType type;
        try {
            type = NotificationType.valueOf(event.severity().toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            log.warn("Invalid or missing severity in ThresholdBreachedEvent, defaulting to WARNING. Value: {}", event.severity());
            type = NotificationType.WARNING;
        }

        SendNotificationCommand command = new SendNotificationCommand(
                type,
                event.message(),
                event.targetUserId(),
                null,
                null,
                null
        );

        commandService.handle(command);
    }
}
