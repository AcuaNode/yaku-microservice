package com.yaku.gateway.iam.infrastructure.authorization.sfs.model;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * UsernamePasswordAuthenticationToken Builder
 */
public class UsernamePasswordAuthenticationTokenBuilder {

    /**
     * Build the token without HTTP request details (WebFlux compatible).
     * @param principal The user details.
     * @return UsernamePasswordAuthenticationToken
     */
    public static UsernamePasswordAuthenticationToken build(UserDetails principal) {
        return new UsernamePasswordAuthenticationToken(
                principal, 
                null, 
                principal.getAuthorities()
        );
    }
}
