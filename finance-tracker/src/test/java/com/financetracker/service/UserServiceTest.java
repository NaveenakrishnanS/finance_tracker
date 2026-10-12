package com.financetracker.service;

import com.financetracker.dto.user.ChangePasswordRequest;
import com.financetracker.dto.user.UpdateProfileRequest;
import com.financetracker.dto.user.UserProfileResponse;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.UserMapper;
import com.financetracker.model.User;
import com.financetracker.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for UserService using JUnit 5 and Mockito.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserMapper userMapper;

    @InjectMocks
    private UserService userService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");
        user.setPasswordHash("hashed");
        user.setDisplayName("Test User");
        user.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void getProfile_returnsUserProfileResponse_forValidUserId() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        var expected = new UserProfileResponse(1L, "test@example.com", "Test User",
                user.getCreatedAt());
        when(userMapper.toProfileResponse(user)).thenReturn(expected);
        var result = userService.getProfile(1L);
        assertThat(result.email()).isEqualTo("test@example.com");
    }

    @Test
    void getProfile_throwsResourceNotFoundException_whenUserNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> userService.getProfile(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void updateDisplayName_updatesAndReturnsUpdatedProfile() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        userService.updateDisplayName(1L, new UpdateProfileRequest("New Name"));
        verify(userRepository).save(any(User.class));
        assertThat(user.getDisplayName()).isEqualTo("New Name");
    }

    @Test
    void changePassword_succeeds_whenCurrentPasswordMatches() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("current", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("newpassword")).thenReturn("newhash");
        userService.changePassword(1L, new ChangePasswordRequest("current", "newpassword"));
        verify(userRepository).save(any(User.class));
    }

    @Test
    void changePassword_throwsForbiddenOperationException_whenPasswordDoesNotMatch() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);
        assertThatThrownBy(() ->
                userService.changePassword(1L, new ChangePasswordRequest("wrong", "new")))
                .isInstanceOf(ForbiddenOperationException.class);
    }

    @Test
    void deleteAccount_callsRepositoryDelete() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        userService.deleteAccount(1L);
        verify(userRepository).delete(user);
    }
}
