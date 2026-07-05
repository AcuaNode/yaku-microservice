package io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform;

import io.github.rafaviv.yakubackend.equipment.domain.model.aggregates.Pond;
import io.github.rafaviv.yakubackend.equipment.interfaces.rest.resources.PondResource;

public class PondResourceFromEntityAssembler {
    public static PondResource toResourceFromEntity(Pond entity) {
        return new PondResource(
                entity.getId(),
                entity.getFarmId(),
                entity.getName(),
                entity.getSpecies() != null ? entity.getSpecies().name() : null,
                entity.getVolume(),
                entity.getStatus().name(),
                entity.getAssignedOperatorId()
        );
    }
}
