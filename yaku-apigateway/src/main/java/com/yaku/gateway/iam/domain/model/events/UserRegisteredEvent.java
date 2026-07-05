package com.yaku.gateway.iam.domain.model.events;

public record UserRegisteredEvent(Long userId, String username, String email, Long farmId) {
}
