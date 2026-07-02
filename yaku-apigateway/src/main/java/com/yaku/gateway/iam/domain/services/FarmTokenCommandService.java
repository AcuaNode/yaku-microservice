package com.yaku.gateway.iam.domain.services;


import java.util.Optional;

import com.yaku.gateway.iam.domain.model.aggregates.FarmToken;
import com.yaku.gateway.iam.domain.model.commands.CreateFarmTokenCommand;

public interface FarmTokenCommandService {
    Optional<FarmToken> handle(CreateFarmTokenCommand command);
}