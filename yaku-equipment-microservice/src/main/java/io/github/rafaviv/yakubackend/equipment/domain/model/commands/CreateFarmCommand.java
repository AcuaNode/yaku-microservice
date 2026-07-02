package io.github.rafaviv.yakubackend.equipment.domain.model.commands;

public record CreateFarmCommand(String name, Long ownerId, String address) {
}
