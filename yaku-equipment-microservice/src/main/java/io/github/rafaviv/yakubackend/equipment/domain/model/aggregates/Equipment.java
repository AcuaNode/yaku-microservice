package io.github.rafaviv.yakubackend.equipment.domain.model.aggregates;

import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentStatus;
import io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects.EquipmentType;
import io.github.rafaviv.yakubackend.shared.domain.model.aggregates.AuditableAbstractAggregateRoot;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "equipment")
public class Equipment extends AuditableAbstractAggregateRoot<Equipment> {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long pondId;

    private Long farmId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EquipmentStatus status;

    @Column(nullable = true)
    private String name;

    @Column(nullable = true)
    private String physicalCode;

    @Column(nullable = true)
    private String address;

    public Equipment() {
    }

    public Equipment(EquipmentType type, String name, String physicalCode, Long farmId) {
        this.type = type;
        this.status = EquipmentStatus.AVAILABLE;
        this.name = name;
        this.physicalCode = physicalCode;
        this.farmId = farmId;
    }

    public void linkToPond(Long pondId) {
        this.pondId = pondId;
        this.status = EquipmentStatus.LINKED;
    }

    public void unlinkFromPond() {
        this.pondId = null;
        this.status = EquipmentStatus.AVAILABLE;
    }
}
