package com.server.app.security;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.server.app.entities.Permission;
import com.server.app.entities.Role;
import com.server.app.entities.User;
import com.server.app.repositories.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import io.jsonwebtoken.JwtException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JsonWebToken jsonWebToken;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JsonWebToken jsonWebToken, UserRepository userRepository) {
        this.jsonWebToken = jsonWebToken;
        this.userRepository = userRepository;
    }

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
        try {
            authenticate(request);
        } catch (BadCredentialsException ex) {
            SecurityErrorResponseWriter.writeJson(response, HttpServletResponse.SC_UNAUTHORIZED, ex.getMessage());
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void authenticate(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            throw new BadCredentialsException("Token requerido");
        }

        String token = header.substring(7);
        Integer userId;
        try {
            userId = jsonWebToken.extractUserId(token);
        } catch (BadCredentialsException | JwtException | IllegalArgumentException ex) {
            throw new BadCredentialsException("Token inválido");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BadCredentialsException("Token inválido"));

        if (user.isBlocked()) {
            throw new BadCredentialsException("Usuario bloqueado");
        }

        Set<SimpleGrantedAuthority> authorities = new HashSet<>();
        Role role = user.getRole();
        if (role != null) {
            if (role.getName() != null) {
                authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
            }

            Collection<Permission> permissions = role.getPermissions();
            if (permissions != null) {
                for (Permission permission : permissions) {
                    authorities.add(
                            new SimpleGrantedAuthority(
                                    permission.getMethod().toUpperCase() + ":" + permission.getPath()
                            )
                    );
                }
            }
        }

        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                user,
                null,
                authorities
        );
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}