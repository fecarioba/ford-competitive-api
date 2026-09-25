package com.fordchallenge.ford_competitive_api.auth.controller;

import com.fordchallenge.ford_competitive_api.auth.dto.AuthResponse;
import com.fordchallenge.ford_competitive_api.auth.dto.LoginRequest;
import com.fordchallenge.ford_competitive_api.auth.dto.RegisterRequest;
import com.fordchallenge.ford_competitive_api.auth.service.AuthService;
import com.fordchallenge.ford_competitive_api.security.AuthenticatedUserProvider;
import com.fordchallenge.ford_competitive_api.users.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@Tag(name = "Auth", description = "Registro, login e dados do usuário autenticado")
public class AuthController {

    private final AuthService authService;
    private final AuthenticatedUserProvider authenticatedUserProvider;

    public AuthController(
            AuthService authService,
            AuthenticatedUserProvider authenticatedUserProvider
    ) {
        this.authService = authService;
        this.authenticatedUserProvider = authenticatedUserProvider;
    }

    @Operation(summary = "Registra um novo usuário (público)")
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@RequestBody @Valid RegisterRequest request) {
        return UserResponse.from(authService.register(request));
    }

    @Operation(summary = "Autentica o usuário e retorna um Bearer Token JWT (público)")
    @PostMapping("/login")
    public AuthResponse login(@RequestBody @Valid LoginRequest request) {
        return authService.login(request);
    }

    @Operation(summary = "Retorna os dados do usuário autenticado (protegido - requer Bearer Token)")
    @GetMapping("/me")
    public ResponseEntity<UserResponse> me() {
        return ResponseEntity.ok(UserResponse.from(authenticatedUserProvider.getCurrentUser()));
    }
}
