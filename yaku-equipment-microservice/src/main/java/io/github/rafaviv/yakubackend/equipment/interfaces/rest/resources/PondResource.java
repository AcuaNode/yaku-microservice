package io.github.rafaviv.yakubackend.equipment.interfaces.rest.resources;

public record PondResource(
        Long id,
        Long farmId,
        String name,
        String species,
        Double volume,
        String status,
        Long assignedOperatorId
) {
}
