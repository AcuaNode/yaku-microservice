package com.yaku.gateway.iam.infrastructure.persistence.jpa.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.yaku.gateway.iam.domain.model.aggregates.FarmToken;

import java.util.Optional;

@Repository
public interface FarmTokenRepository extends JpaRepository<FarmToken, Long> {
    Optional<FarmToken> findByToken(String token);
}