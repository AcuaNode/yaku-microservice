package com.yaku.gateway.iam.application.internal.commandservices;

import com.yaku.gateway.iam.application.internal.outboundservices.hashing.HashingService;
import com.yaku.gateway.iam.application.internal.outboundservices.tokens.TokenService;
import com.yaku.gateway.iam.domain.model.aggregates.User;
import com.yaku.gateway.iam.domain.model.valueobjects.Email;
import com.yaku.gateway.iam.domain.model.valueobjects.HashedPassword;
import com.yaku.gateway.iam.domain.model.commands.SignInCommand;
import com.yaku.gateway.iam.domain.model.commands.SignUpCommand;
import com.yaku.gateway.iam.domain.model.entities.Role;
import com.yaku.gateway.iam.domain.model.exceptions.InvalidCredentialsException;
import com.yaku.gateway.iam.domain.model.exceptions.UserAccountDeactivatedException;
import com.yaku.gateway.iam.domain.model.exceptions.UserAlreadyExistsException;
import com.yaku.gateway.iam.domain.services.RoleValidationService;
import com.yaku.gateway.iam.domain.services.UserCommandService;
import com.yaku.gateway.iam.domain.model.aggregates.FarmToken;
import com.yaku.gateway.iam.infrastructure.persistence.jpa.repositories.FarmTokenRepository;
import com.yaku.gateway.iam.infrastructure.persistence.jpa.repositories.RoleRepository;
import com.yaku.gateway.iam.infrastructure.persistence.jpa.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * User Command Service Implementation
 * <p>
 * This service handles command-based operations for the User aggregate.
 * It implements the UserCommandService interface and provides business logic
 * for user registration and authentication.
 * </p>
 */
@Service
@Transactional
public class UserCommandServiceImpl implements UserCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserCommandServiceImpl.class);

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final HashingService hashingService;
    private final TokenService tokenService;
    private final RoleValidationService roleValidationService;
    private final org.springframework.context.ApplicationEventPublisher eventPublisher;
    private final FarmTokenRepository farmTokenRepository;


    public UserCommandServiceImpl(
            UserRepository userRepository,
            RoleRepository roleRepository,
            HashingService hashingService,
            TokenService tokenService,
            RoleValidationService roleValidationService,
            org.springframework.context.ApplicationEventPublisher eventPublisher,
            FarmTokenRepository farmTokenRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.hashingService = hashingService;
        this.tokenService = tokenService;
        this.roleValidationService = roleValidationService;
        this.eventPublisher = eventPublisher;
        this.farmTokenRepository = farmTokenRepository;
    }

    @Override
    public void handle(SignUpCommand command) {
        LOGGER.info("Processing SignUp command for username: {} with role: {}",
            command.username(), command.requestedRole());

        // Check if user already exists
        if (userRepository.existsByUsername(command.username())) {
            throw new UserAlreadyExistsException(command.username());
        }

        // Validate requested role
        if (!roleValidationService.canRequestRole(command.requestedRole())) {
            throw new IllegalArgumentException("Cannot request role: " + command.requestedRole());
        }

        // Validate FarmToken if role is OPERATOR
        FarmToken farmToken = null;
        if (command.requestedRole() == com.yaku.gateway.iam.domain.model.valueobjects.Roles.OPERATOR) {
            if (command.farmToken() == null || command.farmToken().isBlank()) {
                throw new IllegalArgumentException("Farm token is required for OPERATOR role");
            }
            farmToken = farmTokenRepository.findByToken(command.farmToken())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Farm token"));
            
            if (farmToken.isUsed()) {
                throw new IllegalArgumentException("Farm token is already used");
            }
        }

        // Hash the password
        String hashedPassword = hashingService.encode(command.password());

        // Crear nuevo usuario (solo datos de IAM)
        User user = new User(
                command.username(),
                new Email(command.email()),
                new HashedPassword(hashedPassword),
                command.firstName(),
                command.lastName(),
                false // No verificado por defecto
        );

        // Assign requested role
        Role requestedRole = roleRepository.findByName(command.requestedRole())
                .orElseThrow(() -> new IllegalStateException("Requested role " + command.requestedRole() + " not found"));
        
        user.addRole(requestedRole);
        
        // Assign Farm ID and mark token as used if role is OPERATOR
        Long assignedFarmId = null;
        if (command.requestedRole() == com.yaku.gateway.iam.domain.model.valueobjects.Roles.OPERATOR && farmToken != null) {
            assignedFarmId = farmToken.getFarmId();
            user.setAssignedFarmId(assignedFarmId);
            farmToken.markAsUsed();
            farmTokenRepository.save(farmToken);
        }

        // Save user
        User savedUser = userRepository.save(user);
        LOGGER.info("User registered successfully with ID: {}", savedUser.getId());

        // Publish event
        eventPublisher.publishEvent(new com.yaku.gateway.iam.domain.model.events.UserRegisteredEvent(
                savedUser.getId(), savedUser.getUsername(), savedUser.getEmail().address(), assignedFarmId));
    }

    @Override
    public void handle(SignInCommand command) {
        LOGGER.info("Processing SignIn command for username: {}", command.username());

        // Find user by username
        Optional<User> userOptional = userRepository.findByUsername(command.username());
        if (userOptional.isEmpty()) {
            throw new InvalidCredentialsException();
        }

        User user = userOptional.get();

        // Check if user is active
        if (!user.getActive()) {
            throw new UserAccountDeactivatedException(command.username());
        }

        // Verify password
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

    /**
     * Generate JWT token for authenticated user
     * @param user the authenticated user
     * @return JWT token string
     */
    public String generateTokenForUser(User user) {
        String userRole = user.getRoles().isEmpty() ? "OPERATOR" : 
                         user.getRoles().get(0).getName().name();
        return tokenService.generateToken(user.getId(), userRole);
    }
}
