package io.github.rafaviv.yakubackend.equipment.infrastructure.persistence.jpa.repositories;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Farm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FarmRepository extends JpaRepository<Farm, Long> {
    List<Farm> findByOwnerId(Long ownerId);
}
