package notification_service.notification.interfaces.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import notification_service.notification.domain.models.queries.ListNotificationsQuery;
import notification_service.notification.application.internal.queryservices.ListNotificationsQueryService;
import notification_service.notification.infrastructure.persistance.jpa.NotificationJpaRepository;
import notification_service.notification.interfaces.rest.resources.NotificationResponseResource;
import notification_service.notification.interfaces.rest.transform.NotificationResourceMapper;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/users/{userId}/notifications")
public class NotificationsController {

    private final ListNotificationsQueryService listNotificationsQueryHandler;
    private final NotificationJpaRepository notificationJpaRepository;

    public NotificationsController(ListNotificationsQueryService listNotificationsQueryHandler,
                                   NotificationJpaRepository notificationJpaRepository) {
        this.listNotificationsQueryHandler = listNotificationsQueryHandler;
        this.notificationJpaRepository = notificationJpaRepository;
    }

    @GetMapping
    public ResponseEntity<List<NotificationResponseResource>> getNotificationsByUserId(@PathVariable Long userId) {
        var query = new ListNotificationsQuery(userId);
        var notifications = listNotificationsQueryHandler.handle(query);

        var responseResources = notifications.stream()
                .map(NotificationResourceMapper::toResource)
                .collect(Collectors.toList());

        return ResponseEntity.ok(responseResources);
    }

    @PatchMapping("/read")
    public ResponseEntity<Void> markAllAsRead(@PathVariable Long userId) {
        notificationJpaRepository.markAllAsReadByUserId(userId);
        return ResponseEntity.ok().build();
    }
}
