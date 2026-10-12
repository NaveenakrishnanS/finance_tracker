package com.financetracker.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body for user login.
 *
 * @param email the user's email address
 * @param password the user's password
 */
@Schema(description = "Credentials for user login")
public record LoginRequest(
        @Schema(description = "Registered email address", example = "user@example.com")
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be a valid email address")
        String email,

        @Schema(description = "Account password", example = "secret1234")
        @NotBlank(message = "Password is required")
        String password
) {}
