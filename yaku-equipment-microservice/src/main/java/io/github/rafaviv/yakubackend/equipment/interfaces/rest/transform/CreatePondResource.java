package io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform;

public record CreatePondResource(Long farmId, String name, String species, Double volume) {}
