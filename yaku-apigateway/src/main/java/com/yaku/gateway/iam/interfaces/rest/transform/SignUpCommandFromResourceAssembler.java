package com.yaku.gateway.iam.interfaces.rest.transform;

import com.yaku.gateway.iam.domain.model.commands.SignUpCommand;
import com.yaku.gateway.iam.domain.model.valueobjects.Roles;
import com.yaku.gateway.iam.interfaces.rest.resources.SignUpResource;

/**
 * Assembler for converting SignUpResource to SignUpCommand
 * <p>
 * This class provides static methods to transform data between the interface layer
 * (REST resources) and the domain layer (commands) following DDD principles.
 * </p>
 */
public class SignUpCommandFromResourceAssembler {
    
    /**
     * Converts a SignUpResource to a SignUpCommand
     * @param resource the SignUpResource from the REST request
     * @return SignUpCommand for domain processing
     */
    public static SignUpCommand toCommandFromResource(SignUpResource resource) {
        return new SignUpCommand(
                resource.username(),
                resource.email(),
                resource.password(),
                resource.firstName(),
                resource.lastName(),
                Roles.valueOf(resource.role().toUpperCase()),
                resource.farmToken()
        );
    }
}
