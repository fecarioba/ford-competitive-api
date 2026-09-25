package com.fordchallenge.ford_competitive_api.auth.service;

import com.fordchallenge.ford_competitive_api.auth.dto.AuthResponse;
import com.fordchallenge.ford_competitive_api.auth.dto.LoginRequest;
import com.fordchallenge.ford_competitive_api.auth.dto.RegisterRequest;
import com.fordchallenge.ford_competitive_api.common.exception.EmailAlreadyRegisteredException;
import com.fordchallenge.ford_competitive_api.common.exception.InvalidCredentialsException;
import com.fordchallenge.ford_competitive_api.security.JwtService;
import com.fordchallenge.ford_competitive_api.users.entity.User;
import com.fordchallenge.ford_competitive_api.users.entity.UserRole;
import com.fordchallenge.ford_competitive_api.users.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyRegisteredException("Email já cadastrado: " + request.email());
        }

        User user = User.builder()
                .nome(request.nome())
                .email(request.email())
                .senhaHash(passwordEncoder.encode(request.senha()))
                .role(UserRole.USER)
                .build();

        return userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {

        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new InvalidCredentialsException("Credenciais inválidas"));

        boolean passwordMatches = passwordEncoder.matches(
                request.senha(),
                user.getSenhaHash()
        );

        if (!passwordMatches) {
            throw new InvalidCredentialsException("Credenciais inválidas");
        }

        String token = jwtService.generateToken(user);

        return new AuthResponse(token, "Bearer");
    }
}
