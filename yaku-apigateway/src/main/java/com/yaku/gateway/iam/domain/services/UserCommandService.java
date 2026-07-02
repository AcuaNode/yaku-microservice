package com.yaku.gateway.iam.domain.services;

import com.yaku.gateway.iam.domain.model.commands.SignInCommand;
import com.yaku.gateway.iam.domain.model.commands.SignUpCommand;

public interface UserCommandService {
    void handle(SignUpCommand command);

    void handle(SignInCommand command);

    void changePassword(Long userId, String currentPassword, String newPassword);
}
