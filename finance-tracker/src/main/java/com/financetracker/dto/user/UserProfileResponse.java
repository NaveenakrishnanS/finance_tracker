package com.financetracker.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

/**
 * Response body containing the user's profile information.
 *
 * @param id the user's unique identifier
 * @param email the user's email address
 * @param displayName the user's display name
 * @param createdAt the timestamp when the account was created
 */
@Schema(description = "Authenticated user's profile")
public record UserProfileResponse(
        @Schema(description = "Unique user ID", example = "1")
        Long id,

        @Schema(description = "Email address", example = "user@example.com")
        String email,

        @Schema(description = "Display name shown in the UI", example = "Jane Doe")
        String displayName,

        @Schema(description = "Account creation timestamp")
        LocalDateTime createdAt
) {}
