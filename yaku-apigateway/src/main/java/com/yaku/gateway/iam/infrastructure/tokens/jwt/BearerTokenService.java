package com.yaku.gateway.iam.infrastructure.tokens.jwt;

import com.yaku.gateway.iam.application.internal.outboundservices.tokens.TokenService;

public interface BearerTokenService extends TokenService {

    /**
     * Extracts the JWT token from the raw Authorization header.
     * @param authorizationHeader The Authorization header value.
     * @return String the JWT token
     */
    String getBearerTokenFromHeader(String authorizationHeader);
}