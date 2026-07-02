package notification_service.notification.interfaces.rest;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import notification_service.notification.application.internal.commandservices.RegisterDeviceTokenCommandService;
import notification_service.notification.domain.models.commands.RegisterDeviceTokenCommand;
import notification_service.notification.interfaces.rest.resources.RegisterDeviceTokenRequestResource;


@RestController
@RequestMapping("/api/v1/users/{userId}/device-tokens")
public class DeviceTokensController {

    private final RegisterDeviceTokenCommandService commandHandler;

    public DeviceTokensController(RegisterDeviceTokenCommandService commandHandler) {
        this.commandHandler = commandHandler;
    }

    @PostMapping
    public ResponseEntity<Void> registerToken(
            @PathVariable Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody RegisterDeviceTokenRequestResource resource) {
        if (!isAuthorized(userId, headerUserId)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        var command = new RegisterDeviceTokenCommand(userId, resource.fcmToken());
        commandHandler.handle(command);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    private boolean isAuthorized(Long pathUserId, Long headerUserId) {
        return headerUserId != null && headerUserId.equals(pathUserId);
    }
}
