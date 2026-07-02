package com.yaku.gateway.iam.interfaces;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.yaku.gateway.iam.application.internal.commandservices.UserCommandServiceImpl;
import com.yaku.gateway.iam.application.internal.queryservices.UserQueryServiceImpl;
import com.yaku.gateway.iam.domain.model.aggregates.User;
import com.yaku.gateway.iam.domain.model.commands.SignInCommand;
import com.yaku.gateway.iam.domain.model.commands.SignUpCommand;
import com.yaku.gateway.iam.domain.model.exceptions.InvalidCredentialsException;
import com.yaku.gateway.iam.domain.model.exceptions.UserAccountDeactivatedException;
import com.yaku.gateway.iam.domain.model.exceptions.UserAlreadyExistsException;
import com.yaku.gateway.iam.domain.model.exceptions.UserNotFoundException;
import com.yaku.gateway.iam.domain.model.queries.GetAllUsersQuery;
import com.yaku.gateway.iam.domain.model.queries.GetUserByIdQuery;
import com.yaku.gateway.iam.domain.model.queries.GetUserByUsernameQuery;
import com.yaku.gateway.iam.domain.model.queries.GetUsersByFarmIdQuery;
import com.yaku.gateway.iam.domain.services.RoleValidationService;
import com.yaku.gateway.iam.interfaces.rest.resources.AuthenticationResponseResource;
import com.yaku.gateway.iam.interfaces.rest.resources.ChangePasswordResource;
import com.yaku.gateway.iam.interfaces.rest.resources.SignInResource;
import com.yaku.gateway.iam.interfaces.rest.resources.SignUpResource;
import com.yaku.gateway.iam.interfaces.rest.resources.UserResource;
import com.yaku.gateway.iam.interfaces.rest.transform.SignInCommandFromResourceAssembler;
import com.yaku.gateway.iam.interfaces.rest.transform.SignUpCommandFromResourceAssembler;
import com.yaku.gateway.iam.interfaces.rest.transform.UserResourceFromEntityAssembler;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/users")
public class UsersController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    private final UserCommandServiceImpl userCommandService;
    private final UserQueryServiceImpl userQueryService;
    private final RoleValidationService roleValidationService;

    public UsersController(
            UserCommandServiceImpl userCommandService,
            UserQueryServiceImpl userQueryService,
            RoleValidationService roleValidationService) {
        this.userCommandService = userCommandService;
        this.userQueryService = userQueryService;
        this.roleValidationService = roleValidationService;
    }

    @PostMapping("/signup")
    public Mono<ResponseEntity<String>> signUp(@Valid @RequestBody SignUpResource signUpResource) {
        return Mono.fromCallable(() -> {
            try {
                LOGGER.info("Processing signup request for username: {}", signUpResource.username());
                SignUpCommand command = SignUpCommandFromResourceAssembler.toCommandFromResource(signUpResource);
                userCommandService.handle(command);
                LOGGER.info("User registered successfully: {}", signUpResource.username());
                return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully");
            } catch (UserAlreadyExistsException e) {
                LOGGER.warn("Signup failed: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Signup failed: {}", e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } catch (Exception e) {
                LOGGER.error("Unexpected error during signup", e);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An unexpected error occurred during registration");
            }
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PostMapping("/signin")
    public Mono<ResponseEntity<Object>> signIn(@Valid @RequestBody SignInResource signInResource) {
        return Mono.fromCallable(() -> {
            ResponseEntity<Object> response;
            try {
                LOGGER.info("Processing signin request for username: {}", signInResource.username());
                SignInCommand command = SignInCommandFromResourceAssembler.toCommandFromResource(signInResource);
                userCommandService.handle(command);

                Optional<User> userOptional = userQueryService.handle(new GetUserByUsernameQuery(signInResource.username()));
                if (userOptional.isEmpty()) {
                    response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Authentication failed");
                } else {
                    User user = userOptional.get();
                    String token = userCommandService.generateTokenForUser(user);
                    UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(user);
                    AuthenticationResponseResource authResponse = AuthenticationResponseResource.of(token, 604800L, userResource);
                    LOGGER.info("User authenticated successfully: {}", signInResource.username());
                    response = ResponseEntity.ok(authResponse);
                }
            } catch (InvalidCredentialsException e) {
                LOGGER.warn("Signin failed: {}", e.getMessage());
                response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage());
            } catch (UserAccountDeactivatedException e) {
                LOGGER.warn("Signin failed: {}", e.getMessage());
                response = ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
            } catch (IllegalArgumentException e) {
                LOGGER.warn("Signin failed: {}", e.getMessage());
                response = ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
            } catch (Exception e) {
                LOGGER.error("Unexpected error during signin", e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An unexpected error occurred during authentication");
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/by-username")
    public Mono<ResponseEntity<Object>> getUserByUsername(@RequestParam String username) {
        return Mono.fromCallable(() -> {
            ResponseEntity<Object> response;
            try {
                LOGGER.debug("Processing getUserByUsername: {}", username);
                Optional<User> userOptional = userQueryService.handle(new GetUserByUsernameQuery(username));
                if (userOptional.isEmpty()) {
                    throw new UserNotFoundException(username);
                }
                UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get());
                response = ResponseEntity.ok(userResource);
            } catch (UserNotFoundException e) {
                LOGGER.warn("User not found: {}", username);
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } catch (Exception e) {
                LOGGER.error("Error retrieving user", e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body("An unexpected error occurred");
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<Object>> getUserById(@PathVariable Long id) {
        return Mono.fromCallable(() -> {
            ResponseEntity<Object> response;
            try {
                LOGGER.debug("Processing getUserById: {}", id);
                Optional<User> userOptional = userQueryService.handle(new GetUserByIdQuery(id));
                if (userOptional.isEmpty()) {
                    response = ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found with id: " + id);
                } else {
                    UserResource userResource = UserResourceFromEntityAssembler.toResourceFromEntity(userOptional.get());
                    response = ResponseEntity.ok(userResource);
                }
            } catch (Exception e) {
                LOGGER.error("Error retrieving user by id", e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving user");
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping
    public Mono<ResponseEntity<Object>> getAllUsers(@RequestParam(required = false) Long farmId) {
        return Mono.fromCallable(() -> {
            ResponseEntity<Object> response;
            try {
                LOGGER.debug("Processing getAllUsers. farmId: {}", farmId);
                List<User> users = (farmId != null) 
                        ? userQueryService.handle(new GetUsersByFarmIdQuery(farmId))
                        : userQueryService.handle(new GetAllUsersQuery());
                
                List<UserResource> resources = users.stream()
                        .map(UserResourceFromEntityAssembler::toResourceFromEntity)
                        .toList();
                response = ResponseEntity.ok(resources);
            } catch (Exception e) {
                LOGGER.error("Error retrieving all users", e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving users");
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @GetMapping("/available-roles")
    public Mono<ResponseEntity<Object>> getAvailableRoles() {
        return Mono.fromCallable(() -> {
            ResponseEntity<Object> response;
            try {
                var availableRoles = roleValidationService.getAvailableRolesForRegistration();
                response = ResponseEntity.ok(availableRoles);
            } catch (Exception e) {
                LOGGER.error("Error retrieving roles", e);
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error retrieving roles");
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }

    @PatchMapping("/{id}/password")
    public Mono<ResponseEntity<Object>> changePassword(@PathVariable Long id, @RequestBody ChangePasswordResource resource) {
        return Mono.fromCallable(() -> {
            ResponseEntity<Object> response;
            try {
                userCommandService.changePassword(id, resource.currentPassword(), resource.newPassword());
                response = ResponseEntity.ok().build();
            } catch (InvalidCredentialsException e) {
                response = ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Current password incorrect");
            } catch (IllegalArgumentException e) {
                response = ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
            } catch (Exception e) {
                response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error changing password");
            }
            return response;
        }).subscribeOn(Schedulers.boundedElastic());
    }
}
