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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários de AuthService: cenários de sucesso e de erro
 * (Sprint 3 - critério "Testes Automatizados").
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        authService = new AuthService(userRepository, passwordEncoder, jwtService);
        registerRequest = new RegisterRequest("Victor Hugo", "victor@test.com", "123456");
    }

    @Test
    void deveRegistrarNovoUsuarioComSenhaCriptografada() {
        when(userRepository.existsByEmail("victor@test.com")).thenReturn(false);
        when(passwordEncoder.encode("123456")).thenReturn("hash-fake");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User created = authService.register(registerRequest);

        assertThat(created.getEmail()).isEqualTo("victor@test.com");
        assertThat(created.getSenhaHash()).isEqualTo("hash-fake");
        assertThat(created.getRole()).isEqualTo(UserRole.USER);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void deveRejeitarRegistroComEmailJaCadastrado_conflito409() {
        when(userRepository.existsByEmail("victor@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(EmailAlreadyRegisteredException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void deveAutenticarUsuarioComCredenciaisValidas() {
        User user = User.builder()
                .id(1L)
                .email("victor@test.com")
                .senhaHash("hash-fake")
                .role(UserRole.USER)
                .build();

        LoginRequest loginRequest = new LoginRequest("victor@test.com", "123456");

        when(userRepository.findByEmail("victor@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("123456", "hash-fake")).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn("fake-jwt-token");

        AuthResponse response = authService.login(loginRequest);

        assertThat(response.accessToken()).isEqualTo("fake-jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
    }

    @Test
    void deveRejeitarLoginComSenhaIncorreta_401() {
        User user = User.builder()
                .id(1L)
                .email("victor@test.com")
                .senhaHash("hash-fake")
                .role(UserRole.USER)
                .build();

        LoginRequest loginRequest = new LoginRequest("victor@test.com", "senha-errada");

        when(userRepository.findByEmail("victor@test.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("senha-errada", "hash-fake")).thenReturn(false);

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }

    @Test
    void deveRejeitarLoginComEmailInexistente_401() {
        LoginRequest loginRequest = new LoginRequest("naoexiste@test.com", "123456");

        when(userRepository.findByEmail("naoexiste@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(loginRequest))
                .isInstanceOf(InvalidCredentialsException.class);
    }
}
