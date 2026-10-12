package com.financetracker.controller;

import com.financetracker.dto.user.ChangePasswordRequest;
import com.financetracker.dto.user.UpdateProfileRequest;
import com.financetracker.dto.user.UserProfileResponse;
import com.financetracker.model.User;
import com.financetracker.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for user profile management operations.
 * All endpoints require JWT authentication.
 */
@RestController
@RequestMapping("/api/v1/users")
@Tag(name = "Users", description = "User profile management")
@SecurityRequirement(name = "bearerAuth")
public class UserController {

    private final UserService userService;

    /**
     * Constructs the UserController with the UserService dependency.
     *
     * @param userService the user profile service
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Returns the current user's profile.
     *
     * @param authentication the current security authentication
     * @return 200 OK with the user's profile
     */
    @GetMapping("/me")
    @Operation(summary = "Get current user profile")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile retrieved"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileResponse> getProfile(Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(userService.getProfile(userId));
    }

    /**
     * Updates the current user's display name.
     *
     * @param request the update request
     * @param authentication the current security authentication
     * @return 200 OK with the updated profile
     */
    @PutMapping("/me")
    @Operation(summary = "Update current user display name")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Profile updated"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<UserProfileResponse> updateProfile(
            @Valid @RequestBody UpdateProfileRequest request,
            Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        return ResponseEntity.ok(userService.updateDisplayName(userId, request));
    }

    /**
     * Changes the current user's password.
     *
     * @param request the change password request
     * @param authentication the current security authentication
     * @return 204 No Content on success
     */
    @PutMapping("/me/password")
    @Operation(summary = "Change current user password")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Password changed"),
        @ApiResponse(responseCode = "400", description = "Invalid request"),
        @ApiResponse(responseCode = "401", description = "Unauthorized"),
        @ApiResponse(responseCode = "403", description = "Current password incorrect")
    })
    public ResponseEntity<Void> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        userService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }

    /**
     * Deletes the current user's account and all associated data.
     *
     * @param authentication the current security authentication
     * @return 204 No Content on success
     */
    @DeleteMapping("/me")
    @Operation(summary = "Delete current user account")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Account deleted"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Void> deleteAccount(Authentication authentication) {
        var userId = ((User) authentication.getPrincipal()).getId();
        userService.deleteAccount(userId);
        return ResponseEntity.noContent().build();
    }
}
