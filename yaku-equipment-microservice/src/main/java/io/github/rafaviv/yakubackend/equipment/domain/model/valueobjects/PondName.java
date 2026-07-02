package io.github.rafaviv.yakubackend.equipment.domain.model.valueobjects;

public record PondName(String name) {
    public PondName {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Pond name cannot be null or empty");
        }
        if (name.length() > 100) {
            throw new IllegalArgumentException("Pond name cannot exceed 100 characters");
        }
    }
}
