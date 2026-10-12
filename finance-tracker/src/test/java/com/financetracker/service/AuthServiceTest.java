package com.financetracker.service;

import com.financetracker.dto.auth.LoginRequest;
import com.financetracker.dto.auth.RegisterRequest;
import com.financetracker.exception.DuplicateResourceException;
import com.financetracker.mapper.UserMapper;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import com.financetracker.security.JwtService;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AuthService using JUnit 5 and Mockito.
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtService jwtService;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private AuthService authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed");
        user.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void register_successPath_createsUserWithHashedPassword() {
        var request = new RegisterRequest("test@example.com", "password1", "Test");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("password1")).thenReturn("hashed");
        when(userRepository.save(any(User.class))).thenReturn(user);
        authService.register(request);
        verify(userRepository).save(any(User.class));
        verify(passwordEncoder).encode("password1");
    }

    @Test
    void register_throwsDuplicateResourceException_whenEmailAlreadyExists() {
        var request = new RegisterRequest("test@example.com", "password1", null);
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void login_successPath_returnsTokenResponseWithNonNullToken() {
        var request = new LoginRequest("test@example.com", "password1");
        when(userRepository.findByEmail("test@example.com"))
                .thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");
        when(jwtService.getExpiryMs()).thenReturn(7200000L);
        var response = authService.login(request);
        assertThat(response.token()).isNotNull();
        assertThat(response.token()).isEqualTo("jwt-token");
    }

    @Test
    void login_throwsException_whenCredentialsAreInvalid() {
        var request = new LoginRequest("test@example.com", "wrong");
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));
        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
