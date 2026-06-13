package com.server.app.security;

import java.io.IOException;
import java.util.Collection;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class DynamicAuthorizationFilter extends OncePerRequestFilter {

    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return SecurityRules.isPublic(request) || "OPTIONS".equalsIgnoreCase(request.getMethod());
    }

    @Override
    protected boolean shouldNotFilterAsyncDispatch() {
        return true;
    }

    @Override
    protected boolean shouldNotFilterErrorDispatch() {
        return true;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            SecurityErrorResponseWriter.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, "No autenticado");
            return;
        }

        String requiredAuthorityPrefix = request.getMethod().toUpperCase() + ":";
        String requestPath = request.getRequestURI();

        boolean granted = hasPermission(authentication.getAuthorities(), requiredAuthorityPrefix, requestPath);
        if (!granted) {
            SecurityErrorResponseWriter.writeJson(
                    response,
                    HttpServletResponse.SC_FORBIDDEN,
                    "No tienes permiso para realizar esta petición"
            );
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean hasPermission(
            Collection<? extends GrantedAuthority> authorities,
            String requiredAuthorityPrefix,
            String requestPath
    ) {
        for (GrantedAuthority authority : authorities) {
            String value = authority.getAuthority();
            if (value == null || !value.startsWith(requiredAuthorityPrefix)) {
                continue;
            }

            String storedPattern = value.substring(requiredAuthorityPrefix.length());
            if (matches(storedPattern, requestPath)) {
                return true;
            }
        }

        return false;
    }

    private boolean matches(String storedPattern, String requestPath) {
        String antPattern = storedPattern.replaceAll("\\{[^/]+\\}", "*");
        return pathMatcher.match(antPattern, requestPath);
    }
}