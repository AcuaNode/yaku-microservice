package io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PondRepository extends JpaRepository<Pond, Long> {
    List<Pond> findByFarmId(Long farmId);

    List<Pond> findByAssignedOperatorId(Long assignedOperatorId);
}