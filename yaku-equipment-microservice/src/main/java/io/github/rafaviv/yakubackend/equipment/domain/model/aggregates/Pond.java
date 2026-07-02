package io.github.rafaviv.yakubackend.equipment.domain.model.aggregates;

import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.PondStatus;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.Species;
import io.github.rafaviv.yakubackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "ponds")
public class Pond extends AuditableAbstractAggregateRoot<Pond> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long farmId;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Species species;

    @Column(nullable = false)
    private Double volume;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PondStatus status;

    @Column
    private Long assignedOperatorId;

    public Pond() {
        // JPA requires a default constructor
    }

    public Pond(Long farmId, String name, Species species, Double volume) {
        this.farmId = farmId;
        this.name = name;
        this.species = species;
        this.volume = volume;
        this.status = PondStatus.ACTIVE;
    }
    
    public void updateStatus(PondStatus status) {
        this.status = status;
    }

    public void update(String name, Species species, Double volume) {
        if (name != null && !name.isBlank()) this.name = name;
        if (species != null) this.species = species;
        if (volume != null && volume > 0) this.volume = volume;
    }

    public void assignOperator(Long operatorId) {
        this.assignedOperatorId = operatorId;
    }

    public void deassignOperator() {
        this.assignedOperatorId = null;
    }
}