package com.yaku.gateway.iam.application.internal.commandservices;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.yaku.gateway.iam.domain.model.aggregates.FarmToken;
import com.yaku.gateway.iam.domain.model.commands.CreateFarmTokenCommand;
import com.yaku.gateway.iam.domain.services.FarmTokenCommandService;
import com.yaku.gateway.iam.infrastructure.persistence.jpa.repositories.FarmTokenRepository;

import java.util.Optional;

@Service
@Transactional
public class FarmTokenCommandServiceImpl implements FarmTokenCommandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FarmTokenCommandServiceImpl.class);

    private final FarmTokenRepository farmTokenRepository;

    public FarmTokenCommandServiceImpl(FarmTokenRepository farmTokenRepository) {
        this.farmTokenRepository = farmTokenRepository;
    }

    @Override
    public Optional<FarmToken> handle(CreateFarmTokenCommand command) {
        LOGGER.info("Creating farm token for farm ID: {}", command.farmId());
        FarmToken farmToken = new FarmToken(command.farmId());
        FarmToken saved = farmTokenRepository.save(farmToken);
        LOGGER.info("Farm token created with token: {}", saved.getToken());
        return Optional.of(saved);
    }
}