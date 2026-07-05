package com.yaku.gateway.iam.domain.services;

import com.yaku.gateway.iam.domain.model.aggregates.FarmToken;
import com.yaku.gateway.iam.domain.model.commands.CreateFarmTokenCommand;

import java.util.Optional;

public interface FarmTokenCommandService {
    Optional<FarmToken> handle(CreateFarmTokenCommand command);
}
