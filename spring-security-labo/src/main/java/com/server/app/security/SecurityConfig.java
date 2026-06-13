package com.server.app.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final DynamicAuthorizationFilter dynamicAuthorizationFilter;

    public SecurityConfig(
            JwtAuthenticationFilter jwtAuthenticationFilter,
            DynamicAuthorizationFilter dynamicAuthorizationFilter
    ) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.dynamicAuthorizationFilter = dynamicAuthorizationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        AuthenticationEntryPoint entryPoint = (request, response, authException) ->
                SecurityErrorResponseWriter.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED,
                        authException.getMessage() != null ? authException.getMessage() : "No autenticado");

        AccessDeniedHandler deniedHandler = (request, response, accessDeniedException) ->
                SecurityErrorResponseWriter.writeJson(response, HttpServletResponse.SC_FORBIDDEN,
                        accessDeniedException.getMessage() != null && !accessDeniedException.getMessage().isBlank()
                                ? accessDeniedException.getMessage()
                                : "No tienes permiso para realizar esta petición");

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(SecurityRules.publicPaths()).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex.authenticationEntryPoint(entryPoint).accessDeniedHandler(deniedHandler))
                .addFilterBefore(jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(dynamicAuthorizationFilter, JwtAuthenticationFilter.class)
                .build();
    }
}