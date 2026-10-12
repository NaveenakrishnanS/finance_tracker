package com.financetracker.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Response body containing the JWT access token after successful authentication.
 *
 * @param token the signed JWT access token
 * @param expiresIn the token expiry duration in milliseconds
 */
@Schema(description = "JWT access token issued after successful authentication")
public record TokenResponse(
        @Schema(description = "Signed JWT bearer token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
        String token,

        @Schema(description = "Token lifetime in milliseconds", example = "7200000")
        long expiresIn
) {}
