package com.yaku.gateway.iam.interfaces.rest.transform;

import com.yaku.gateway.iam.domain.model.commands.SignInCommand;
import com.yaku.gateway.iam.interfaces.rest.resources.SignInResource;

public class SignInCommandFromResourceAssembler {
    public static SignInCommand toCommandFromResource(SignInResource resource) {
        return new SignInCommand(
                resource.username(),
                resource.password()
        );
    }
}
