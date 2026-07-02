package notification_service.notification.infrastructure.persistance.jpa;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import notification_service.notification.domain.models.valueobjects.NotificationType;
import notification_service.notification.domain.models.valueobjects.SensorType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter
@Setter
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(nullable = false, length = 500)
    private String message;

    @Column(nullable = false)
    private Long recipientUserId;

    private BigDecimal triggerValue;

    @Enumerated(EnumType.STRING)
    private SensorType triggerSensorType;

    private String triggerHardwareStatus;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean isRead = false;
}