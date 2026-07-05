package notification_service.notification.domain.models.aggregates;

import java.time.LocalDateTime;
import notification_service.notification.domain.models.valueobjects.NotificationType;
import notification_service.notification.domain.models.valueobjects.RecipientInfo;
import notification_service.notification.domain.models.valueobjects.TriggerSnapshot;

public class Notification {
    private Long id;
    private final NotificationType type;
    private final String message;
    private final RecipientInfo recipient;
    private final TriggerSnapshot triggerData;
    private final LocalDateTime createdAt;

    public Notification(NotificationType type, String message, RecipientInfo recipient, TriggerSnapshot triggerData) {
        if (type == null) throw new IllegalArgumentException("NotificationType cannot be null");
        if (message == null || message.isBlank()) throw new IllegalArgumentException("Message cannot be blank");
        if (recipient == null) throw new IllegalArgumentException("Recipient cannot be null");

        this.type = type;
        this.message = message;
        this.recipient = recipient;
        this.triggerData = triggerData;
        this.createdAt = LocalDateTime.now();
    }

    public void setId(Long id) {
        if (this.id != null) throw new IllegalStateException("ID is already set");
        this.id = id;
    }

    public Long getId() { return id; }
    public NotificationType getType() { return type; }
    public String getMessage() { return message; }
    public RecipientInfo getRecipient() { return recipient; }
    public TriggerSnapshot getTriggerData() { return triggerData; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
