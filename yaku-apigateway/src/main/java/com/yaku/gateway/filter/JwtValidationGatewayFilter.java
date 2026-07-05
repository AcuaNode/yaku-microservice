package com.yaku.gateway.filter;

import com.yaku.gateway.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtValidationGatewayFilter extends OncePerRequestFilter {

    private static final Logger LOGGER = LoggerFactory.getLogger(JwtValidationGatewayFilter.class);

    private final JwtUtil jwtUtil;
    private final List<String> publicPaths;

    public JwtValidationGatewayFilter(
            JwtUtil jwtUtil,
            @Value("${gateway.public-paths:/api/v1/users/signup,/api/v1/users/signin,/api/v1/users/available-roles,/v3/api-docs,/swagger-ui,/swagger-ui.html,/swagger-resources,/webjars,/error,/actuator}") List<String> publicPaths) {
        this.jwtUtil = jwtUtil;
        this.publicPaths = publicPaths;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();

        if (request.getMethod().equals("OPTIONS")) {
            LOGGER.debug("Skipping JWT validation for OPTIONS preflight");
            filterChain.doFilter(request, response);
            return;
        }

        if (isPublicPath(path)) {
            LOGGER.debug("Skipping JWT validation for public path: {}", path);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            LOGGER.warn("Missing or invalid Authorization header for path: {}", path);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String token = authHeader.substring(7);
        if (!jwtUtil.isTokenValid(token)) {
            LOGGER.warn("Invalid or expired JWT for path: {}", path);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        String userId = jwtUtil.getUserIdFromToken(token);
        String role = jwtUtil.getRoleFromToken(token);

        if (userId == null) {
            LOGGER.warn("JWT missing 'sub' claim for path: {}", path);
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            return;
        }

        LOGGER.debug("JWT validated for userId={} role={} path={}", userId, role, path);

        MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
        mutableRequest.putHeader("X-User-Id", userId);
        mutableRequest.putHeader("X-User-Role", role != null ? role : "");

        filterChain.doFilter(mutableRequest, response);
    }

    private boolean isPublicPath(String path) {
        return publicPaths.stream().anyMatch(path::startsWith);
    }

    public static class MutableHttpServletRequest extends HttpServletRequestWrapper {
        private final Map<String, String> customHeaders;

        public MutableHttpServletRequest(HttpServletRequest request) {
            super(request);
            this.customHeaders = new ConcurrentHashMap<>();
        }

        public void putHeader(String name, String value) {
            this.customHeaders.put(name, value);
        }

        @Override
        public String getHeader(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return headerValue;
            }
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            List<String> names = Collections.list(super.getHeaderNames());
            names.addAll(customHeaders.keySet());
            return Collections.enumeration(names);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            String headerValue = customHeaders.get(name);
            if (headerValue != null) {
                return Collections.enumeration(Collections.singletonList(headerValue));
            }
            return super.getHeaders(name);
        }
    }
}
