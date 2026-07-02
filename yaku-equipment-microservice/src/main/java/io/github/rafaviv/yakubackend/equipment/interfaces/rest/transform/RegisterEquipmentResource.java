package io.github.rafaviv.yakubackend.equipment.interfaces.rest.transform;

public record RegisterEquipmentResource(String type, String name, String physicalCode, Long farmId) {}
