package com.financetracker.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for user registration.
 *
 * @param email the user's email address
 * @param password the user's desired password (minimum 8 characters)
 * @param displayName optional display name for the user
 */
@Schema(description = "New user registration details")
public record RegisterRequest(
        @Schema(description = "Email address (must be unique)", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @Schema(description = "Password (minimum 8 characters)", example = "secret1234")
        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        String password,

        @Schema(description = "Optional display name shown in the UI", example = "Jane Doe")
        String displayName
) {}
