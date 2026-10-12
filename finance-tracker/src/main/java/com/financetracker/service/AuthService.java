package com.financetracker.service;

import com.financetracker.dto.auth.LoginRequest;
import com.financetracker.dto.auth.RegisterRequest;
import com.financetracker.dto.auth.TokenResponse;
import com.financetracker.dto.user.UserProfileResponse;
import com.financetracker.exception.DuplicateResourceException;
import com.financetracker.mapper.UserMapper;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import com.financetracker.security.JwtService;
import java.time.LocalDateTime;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user authentication and registration.
 */
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserMapper userMapper;

    /**
     * Constructs AuthService with all required dependencies.
     *
     * @param userRepository the user data repository
     * @param passwordEncoder the BCrypt password encoder
     * @param authenticationManager the Spring Security authentication manager
     * @param jwtService the JWT token service
     * @param userMapper the user entity to DTO mapper
     */
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtService jwtService, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userMapper = userMapper;
    }

    /**
     * Registers a new user account.
     *
     * @param request the registration request containing email, password, and optional display name
     * @return the newly created user's profile response
     * @throws DuplicateResourceException if the email is already registered
     */
    @Transactional
    public UserProfileResponse register(RegisterRequest request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new DuplicateResourceException(
                    "Email already registered: " + request.email());
        }
        var user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setDisplayName(request.displayName());
        user.setCreatedAt(LocalDateTime.now());
        var saved = userRepository.save(user);
        return userMapper.toProfileResponse(saved);
    }

    /**
     * Authenticates a user and returns a JWT token.
     *
     * @param request the login request containing email and password
     * @return a TokenResponse containing the signed JWT and expiry duration
     */
    public TokenResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password()));
        var user = userRepository.findByEmail(request.email())
                .orElseThrow();
        var token = jwtService.generateToken(user);
        return new TokenResponse(token, jwtService.getExpiryMs());
    }
}
