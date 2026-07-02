package com.yaku.gateway.iam.infrastructure.authorization.sfs.configuration;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UserDetailsRepositoryReactiveAuthenticationManager;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import com.yaku.gateway.iam.infrastructure.authorization.sfs.model.UserDetailsServiceExtension;
import com.yaku.gateway.iam.infrastructure.authorization.sfs.pipeline.BearerAuthenticationWebFilter;
import com.yaku.gateway.iam.infrastructure.hashing.bcrypt.BCryptHashingService;
import com.yaku.gateway.iam.infrastructure.tokens.jwt.BearerTokenService;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class WebSecurityConfiguration {

    private final UserDetailsServiceExtension userDetailsService;
    private final BearerTokenService tokenService;
    private final BCryptHashingService hashingService;
    private final ServerAuthenticationEntryPoint unauthorizedRequestHandler;

    public WebSecurityConfiguration(
            @Qualifier("defaultUserDetailsService") UserDetailsServiceExtension userDetailsService,
            BearerTokenService tokenService,
            BCryptHashingService hashingService,
            ServerAuthenticationEntryPoint authenticationEntryPoint
    ) {
        this.userDetailsService = userDetailsService;
        this.tokenService = tokenService;
        this.hashingService = hashingService;
        this.unauthorizedRequestHandler = authenticationEntryPoint;
    }

    private WebFilter bearerAuthenticationWebFilter() {
        return new BearerAuthenticationWebFilter(tokenService, userDetailsService);
    }

    @Bean
    public ReactiveUserDetailsService reactiveUserDetailsService() {
        return username -> Mono.fromCallable(() -> userDetailsService.loadUserByUsername(username))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Bean
    public ReactiveAuthenticationManager authenticationManager() {
        UserDetailsRepositoryReactiveAuthenticationManager provider = 
                new UserDetailsRepositoryReactiveAuthenticationManager(reactiveUserDetailsService());
        provider.setPasswordEncoder(hashingService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return hashingService;
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        http
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfig = new CorsConfiguration();
                    corsConfig.setAllowedOriginPatterns(List.of("*"));
                    corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
                    corsConfig.setAllowedHeaders(List.of("Authorization", "Cache-Control", "Content-Type"));
                    corsConfig.setAllowCredentials(true);
                    return corsConfig;
                }))
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(ex -> ex.authenticationEntryPoint(unauthorizedRequestHandler))
                .authorizeExchange(auth -> auth
                        .pathMatchers(org.springframework.http.HttpMethod.OPTIONS, "/**").permitAll()
                        .pathMatchers("/api/v1/users/signup", "/api/v1/users/signup/**").permitAll()
                        .pathMatchers("/api/v1/users/signin", "/api/v1/users/signin/**").permitAll()
                        .pathMatchers(
                                "/api/v1/users/available-roles",
                                "/api/v1/plans",
                                "/api/v1/plans/**",
                                "/v3/api-docs/**",
                                "/signup",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/error"
                        ).permitAll()
                        .anyExchange().authenticated()
                )
                .authenticationManager(authenticationManager())
                .addFilterAt(bearerAuthenticationWebFilter(), SecurityWebFiltersOrder.AUTHENTICATION);

        return http.build();
    }
}
