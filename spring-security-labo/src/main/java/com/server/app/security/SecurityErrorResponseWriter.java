package com.server.app.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.app.dto.response.ExceptionResponse;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;

final class SecurityErrorResponseWriter {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private SecurityErrorResponseWriter() {
    }

    static void writeJson(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        OBJECT_MAPPER.writeValue(response.getWriter(), new ExceptionResponse(status, message));
    }
}