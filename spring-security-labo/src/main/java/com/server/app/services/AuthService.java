package com.server.app.services;

import com.server.app.dto.auth.AuthResponse;
import com.server.app.dto.auth.LoginDto;
import com.server.app.dto.auth.UpdatePasswordDto;
import com.server.app.dto.auth.UpdateProfileDto;
import com.server.app.dto.user.UserCreateDto;
import com.server.app.entities.User;
import com.server.app.exceptions.NotFoundException;
import com.server.app.exceptions.UnauthorizedException;
import com.server.app.repositories.UserRepository;
import com.server.app.security.JsonWebToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JsonWebToken jsonWebToken;
    private final UserService userService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JsonWebToken jsonWebToken,
            UserService userService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jsonWebToken = jsonWebToken;
        this.userService = userService;
    }

    @Transactional
    public AuthResponse login(LoginDto dto) {
        User user = userRepository.findUserByUsername(dto.getUsername())
                .orElseThrow(() -> new UnauthorizedException("Credenciales inválidas"));

        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Credenciales inválidas");
        }

        if (user.isBlocked()) {
            throw new UnauthorizedException("Usuario bloqueado");
        }

        return new AuthResponse(jsonWebToken.generateToken(user), user);
    }

    @Transactional
    public AuthResponse signup(UserCreateDto dto) {
        User user = userService.create(dto);
        return new AuthResponse(jsonWebToken.generateToken(user), user);
    }

    @Transactional(readOnly = true)
    public User profile(int userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    @Transactional
    public User updateProfile(int userId, UpdateProfileDto dto) {
        return userService.updateProfile(userId, dto);
    }

    @Transactional
    public User updatePassword(int userId, UpdatePasswordDto dto) {
        return userService.updatePassword(userId, dto);
    }
}