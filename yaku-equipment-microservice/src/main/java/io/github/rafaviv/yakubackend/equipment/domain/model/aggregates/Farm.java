package io.github.rafaviv.yakubackend.equipment.domain.model.aggregates;

import io.github.rafaviv.yakubackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.UUID;

@Entity
@Getter
@Table(name = "farms")
public class Farm extends AuditableAbstractAggregateRoot<Farm> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long ownerId; // Equivalent to ADMIN ID

    @Column
    private String address;

    @Column(nullable = false)
    private String farmToken;

    public Farm() {
        // JPA requires a default constructor
    }

    public Farm(String name, Long ownerId, String address) {
        this.name = name;
        this.ownerId = ownerId;
        this.address = address;
        this.farmToken = UUID.randomUUID().toString();
    }

    public void regenerateFarmToken() {
        this.farmToken = UUID.randomUUID().toString();
    }
}
