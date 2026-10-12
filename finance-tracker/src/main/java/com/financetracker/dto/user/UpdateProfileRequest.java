package com.financetracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for updating a user's display name.
 *
 * @param displayName the new display name (must not be blank)
 */
@Schema(description = "Request to update the authenticated user's display name")
public record UpdateProfileRequest(
        @Schema(description = "New display name (must not be blank)", example = "Jane Doe")
        @NotBlank(message = "Display name is required")
        String displayName
) {}
