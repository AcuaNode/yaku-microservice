package com.yaku.gateway.iam.application.internal.commandservices;

import com.yaku.gateway.iam.domain.model.aggregates.FarmToken;
import com.yaku.gateway.iam.domain.model.commands.CreateFarmTokenCommand;
import com.yaku.gateway.iam.domain.services.FarmTokenCommandService;
import com.yaku.gateway.iam.infrastructure.persistence.jpa.repositories.FarmTokenRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class FarmTokenCommandServiceImpl implements FarmTokenCommandService {

    private final FarmTokenRepository farmTokenRepository;

    public FarmTokenCommandServiceImpl(FarmTokenRepository farmTokenRepository) {
        this.farmTokenRepository = farmTokenRepository;
    }

    @Override
    public Optional<FarmToken> handle(CreateFarmTokenCommand command) {
        FarmToken farmToken = new FarmToken(command.farmId());
        return Optional.of(farmTokenRepository.save(farmToken));
    }
}
