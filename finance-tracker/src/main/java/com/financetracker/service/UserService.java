package com.financetracker.service;

import com.financetracker.dto.user.ChangePasswordRequest;
import com.financetracker.dto.user.UpdateProfileRequest;
import com.financetracker.dto.user.UserProfileResponse;
import com.financetracker.exception.ForbiddenOperationException;
import com.financetracker.exception.ResourceNotFoundException;
import com.financetracker.mapper.UserMapper;
import com.financetracker.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service handling user profile management operations.
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    /**
     * Constructs UserService with required dependencies.
     *
     * @param userRepository the user data repository
     * @param passwordEncoder the BCrypt password encoder
     * @param userMapper the user entity to DTO mapper
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                       UserMapper userMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    /**
     * Retrieves a user's profile by their ID.
     *
     * @param userId the ID of the user to retrieve
     * @return the user's profile response
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    public UserProfileResponse getProfile(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        return userMapper.toProfileResponse(user);
    }

    /**
     * Updates a user's display name.
     *
     * @param userId the ID of the user to update
     * @param request the update request containing the new display name
     * @return the updated user's profile response
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    @Transactional
    public UserProfileResponse updateDisplayName(Long userId,
                                                 UpdateProfileRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        user.setDisplayName(request.displayName());
        var saved = userRepository.save(user);
        return userMapper.toProfileResponse(saved);
    }

    /**
     * Changes a user's password after verifying the current password.
     *
     * @param userId the ID of the user
     * @param request the change password request with current and new passwords
     * @throws ResourceNotFoundException if no user exists with the given ID
     * @throws ForbiddenOperationException if the current password does not match
     */
    @Transactional
    public void changePassword(Long userId, ChangePasswordRequest request) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new ForbiddenOperationException("Current password is incorrect.");
        }
        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
    }

    /**
     * Deletes a user account and all associated data.
     *
     * @param userId the ID of the user to delete
     * @throws ResourceNotFoundException if no user exists with the given ID
     */
    @Transactional
    public void deleteAccount(Long userId) {
        var user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
        userRepository.delete(user);
    }
}
