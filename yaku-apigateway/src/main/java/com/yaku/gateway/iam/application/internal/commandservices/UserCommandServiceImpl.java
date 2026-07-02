package com.yaku.gateway.iam.application.internal.commandservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yaku.gateway.iam.application.internal.outboundservices.hashing.HashingService;
import com.yaku.gateway.iam.application.internal.outboundservices.tokens.TokenService;
import com.yaku.gateway.iam.domain.model.aggregates.FarmToken;
import com.yaku.gateway.iam.domain.model.aggregates.User;
import com.yaku.gateway.iam.domain.model.commands.SignInCommand;
import com.yaku.gateway.iam.domain.model.commands.SignUpCommand;
import com.yaku.gateway.iam.domain.model.entities.Role;
import com.yaku.gateway.iam.domain.model.events.UserRegisteredEvent;
import com.yaku.gateway.iam.domain.model.exceptions.InvalidCredentialsException;
import com.yaku.gateway.iam.domain.model.exceptions.UserAccountDeactivatedException;
import com.yaku.gateway.iam.domain.model.exceptions.UserAlreadyExistsException;
import com.yaku.gateway.iam.domain.model.valueobjects.Email;
import com.yaku.gateway.iam.domain.model.valueobjects.HashedPassword;
import com.yaku.gateway.iam.domain.model.valueobjects.Roles;
import com.yaku.gateway.iam.domain.services.RoleValidationService;
import com.yaku.gateway.iam.domain.services.UserCommandService;
import com.yaku.gateway.iam.infrastructure.events.kafka.KafkaDomainEventPublisher;
import com.yaku.gateway.iam.infrastructure.persistence.jpa.repositories.FarmTokenRepository;
import com.yaku.gateway.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.yaku.gateway.iam.infrastructure.persistence.jpa.repositories.UserRepository;

import java.util.Optional;

@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCommandServiceImpl.class);
    private static final String USER_REGISTERED_TOPIC = "iam.user-registered";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleValidationService roleValidationService;
    private final KafkaDomainEventPublisher kafkaDomainEventPublisher;
    private final FarmTokenRepository farmTokenRepository;

    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleValidationService roleValidationService,
            KafkaDomainEventPublisher kafkaDomainEventPublisher,
            FarmTokenRepository farmTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleValidationService = roleValidationService;
        this.kafkaDomainEventPublisher = kafkaDomainEventPublisher;
        this.farmTokenRepository = farmTokenRepository;
    }

    @Override
    public void handle(SignUpCommand command) {
        LOGGER.info("Processing SignUp command for username: {} with role: {}",
            command.username(), command.requestedRole());

        if (userRepository.existsByUsername(command.username())) {
            throw new UserAlreadyExistsException(command.username());
        }

        if (!roleValidationService.canRequestRole(command.requestedRole())) {
            throw new IllegalArgumentException("Cannot request role: " + command.requestedRole());
        }

        String hashedPassword = hashingService.encode(command.password());

        User user = new User(
                command.username(),
                new Email(command.email()),
                new HashedPassword(hashedPassword),
                command.firstName(),
                command.lastName(),
                false
        );

        Long assignedFarmId = null;
        if (command.requestedRole() == Roles.OPERATOR) {
            if (command.farmToken() == null || command.farmToken().isBlank()) {
                throw new IllegalArgumentException("Farm token is required for OPERATOR role");
            }
            FarmToken farmToken = farmTokenRepository.findByToken(command.farmToken())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Farm token"));
            if (farmToken.isUsed()) {
                throw new IllegalArgumentException("Farm token is already used");
            }
            assignedFarmId = farmToken.getFarmId();
            user.setAssignedFarmId(assignedFarmId);
            farmToken.markAsUsed();
            farmTokenRepository.save(farmToken);
        }

        Role requestedRole = roleRepository.findByName(command.requestedRole())
                .orElseThrow(() -> new IllegalStateException("Requested role " + command.requestedRole() + " not found"));

        user.addRole(requestedRole);

        User savedUser = userRepository.save(user);
        LOGGER.info("User registered successfully with ID: {}", savedUser.getId());

        UserRegisteredEvent event = new UserRegisteredEvent(
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail().address(), command.farmToken());
        kafkaDomainEventPublisher.publish(USER_REGISTERED_TOPIC, event);
        LOGGER.info("Published UserRegisteredEvent to Kafka topic '{}': userId={}", USER_REGISTERED_TOPIC, savedUser.getId());
    }

    @Override
    public void handle(SignInCommand command) {
        LOGGER.info("Processing SignIn command for username: {}", command.username());

        Optional<User> userOptional = userRepository.findByUsername(command.username());
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User user = userOptional.get();

        if (!user.getActive()) {
            throw new UserAccountDeactivatedException(command.username());
        }

        if (!hashingService.matches(command.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        LOGGER.info("User authenticated successfully with ID: {}", user.getId());
    }

    @Override
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!hashingService.matches(currentPassword, user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        user.updatePassword(new HashedPassword(hashingService.encode(newPassword)));
        userRepository.save(user);
    }

    public String generateTokenForUser(User user) {
        String userRole = user.getRoles().isEmpty() ? "OPERATOR" :
                         user.getRoles().get(0).getName().name();
        return tokenService.generateToken(user.getId(), userRole);
    }
}