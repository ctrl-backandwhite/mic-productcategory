package com.backandwhite.application.configuration;

import com.backandwhite.common.constants.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Set;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

public class NxRequestFilter extends OncePerRequestFilter {

    private static final Set<String> SKIP_PREFIXES = Set.of("/actuator", "/v3/api-docs", "/swagger-ui",
            "/swagger-ui.html");

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {

        hardenResponseHeaders(response);

        String path = request.getRequestURI();
        if (!shouldValidate(path)) {
            filterChain.doFilter(request, response);
            return;
        }

        String nxHeader = request.getHeader(AppConstants.HEADER_NX036_AUTH);
        if (nxHeader == null || nxHeader.isBlank()) {
            response.setStatus(HttpStatus.FORBIDDEN.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.getWriter().write("{\"error\":\"Forbidden\",\"message\":\"Missing required header\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean shouldValidate(String path) {
        if (!path.startsWith("/api/")) {
            return false;
        }
        for (String prefix : SKIP_PREFIXES) {
            if (path.startsWith(prefix)) {
                return false;
            }
        }
        return true;
    }

    private void hardenResponseHeaders(HttpServletResponse response) {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("X-XSS-Protection", "0");
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        response.setHeader("Permissions-Policy", "geolocation=(), camera=(), microphone=()");
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
    }
}
