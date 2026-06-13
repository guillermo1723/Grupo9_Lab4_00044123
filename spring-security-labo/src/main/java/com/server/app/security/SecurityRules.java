package com.server.app.security;

import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

public final class SecurityRules {

    public static final List<String> PUBLIC_PATHS = List.of(
            "/api/auth/login",
            "/api/auth/signup",
            "/error"
    );

    private SecurityRules() {
    }

    public static boolean isPublic(HttpServletRequest request) {
        return PUBLIC_PATHS.contains(request.getRequestURI());
    }

    public static String[] publicPaths() {
        return PUBLIC_PATHS.toArray(String[]::new);
    }
}