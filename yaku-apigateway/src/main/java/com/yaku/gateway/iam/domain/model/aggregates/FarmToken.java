package com.yaku.gateway.iam.domain.model.aggregates;

import com.yaku.gateway.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
@Table(name = "farm_tokens")
public class FarmToken extends AuditableAbstractAggregateRoot<FarmToken> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String token;

    @Column(nullable = false)
    private Long farmId;

    @Column(nullable = false)
    private boolean isUsed;

        public Long getId() { return id; }
    public String getToken() { return token; }
    public Long getFarmId() { return farmId; }
    public boolean isUsed() { return isUsed; }

    public FarmToken() {
    }

    public FarmToken(Long farmId) {
        this.token = UUID.randomUUID().toString();
        this.farmId = farmId;
        this.isUsed = false;
    }

    public void markAsUsed() {
        if (this.isUsed) {
            throw new IllegalStateException("Token is already used");
        }
        this.isUsed = true;
    }
}
