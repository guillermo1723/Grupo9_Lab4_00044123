package com.server.app.dto.auth;

import com.server.app.entities.User;

public record AuthResponse(
        String token,
        User data
) {}