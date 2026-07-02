package com.yaku.gateway.iam.infrastructure.authorization.sfs.pipeline;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.yaku.gateway.iam.infrastructure.authorization.sfs.model.UserDetailsServiceExtension;
import com.yaku.gateway.iam.infrastructure.tokens.jwt.BearerTokenService;

public class BearerAuthenticationWebFilter implements WebFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(BearerAuthenticationWebFilter.class);

    private final BearerTokenService tokenService;
    private final UserDetailsServiceExtension userDetailsService;

    public BearerAuthenticationWebFilter(BearerTokenService tokenService, UserDetailsServiceExtension userDetailsService) {
        this.tokenService = tokenService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        LOGGER.debug("Incoming request to: {}", path);

        if (exchange.getRequest().getMethod().name().equals("OPTIONS")) {
            LOGGER.debug("🟢 OPTIONS preflight request, skipping JWT validation");
            return chain.filter(exchange);
        }

        if (isPublicPath(path)) {
            LOGGER.debug("🟢 Public path ({}), skipping JWT validation", path);
            return chain.filter(exchange);
        }

        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        String token = tokenService.getBearerTokenFromHeader(authHeader);

        if (StringUtils.hasText(token)) {
            try {
                if (tokenService.validateToken(token)) {
                    Long userId = tokenService.getUserIdFromToken(token);
                    
                    // Offload blocking JPA user lookup to boundedElastic thread pool
                    return Mono.fromCallable(() -> userDetailsService.loadUserById(userId))
                            .subscribeOn(Schedulers.boundedElastic())
                            .flatMap(userDetails -> {
                                var authenticationToken = new UsernamePasswordAuthenticationToken(
                                        userDetails, null, userDetails.getAuthorities());
                                
                                return chain.filter(exchange)
                                        .contextWrite(ReactiveSecurityContextHolder.withAuthentication(authenticationToken));
                            })
                            .onErrorResume(e -> {
                                LOGGER.warn("Authentication failed: {}", e.getMessage());
                                return chain.filter(exchange);
                            });
                }
            } catch (Exception e) {
                LOGGER.warn("Invalid JWT token for request to {}: {}", path, e.getMessage());
            }
        }

        return chain.filter(exchange);
    }

    private boolean isPublicPath(String path) {
        return path.contains("/api/v1/users/signup") ||
               path.contains("/api/v1/users/signin") ||
               path.contains("/api/v1/users/available-roles") ||
               path.contains("/api/v1/plans") ||
               path.contains("/v3/api-docs") ||
               path.contains("/swagger-ui") ||
               path.contains("/swagger-resources") ||
               path.contains("/webjars");
    }
}
