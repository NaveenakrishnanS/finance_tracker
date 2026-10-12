package com.financetracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for changing a user's password.
 *
 * @param currentPassword the user's current password for verification
 * @param newPassword the new password (minimum 8 characters)
 */
@Schema(description = "Request to change the authenticated user's password")
public record ChangePasswordRequest(
        @Schema(description = "Current password for verification", example = "oldSecret1234")
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @Schema(description = "New password (minimum 8 characters)", example = "newSecret5678")
        @NotBlank(message = "New password is required")
        @Size(min = 8, message = "New password must be at least 8 characters")
        String newPassword
) {}
