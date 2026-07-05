package com.yaku.gateway.iam.interfaces.rest.resources;

public record FarmTokenResource(Long id, String token, Long farmId, boolean isUsed) {
}
