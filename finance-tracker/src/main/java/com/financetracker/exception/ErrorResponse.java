package com.financetracker.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response body returned by the API for all error conditions.
 *
 * @param timestamp the time the error occurred
 * @param status the HTTP status code
 * @param error the HTTP status reason phrase
 * @param message a human-readable error message
 * @param path the request path that caused the error
 * @param fieldErrors optional list of field-level validation errors
 */
@Schema(description = "Standard error response")
public record ErrorResponse(
        @Schema(description = "Timestamp when the error occurred") LocalDateTime timestamp,
        @Schema(description = "HTTP status code") int status,
        @Schema(description = "HTTP status reason phrase") String error,
        @Schema(description = "Human-readable error message") String message,
        @Schema(description = "Request path") String path,
        @Schema(description = "Field-level validation errors") List<FieldError> fieldErrors
) {

    /**
     * Convenience constructor without field errors.
     *
     * @param timestamp the time the error occurred
     * @param status the HTTP status code
     * @param error the HTTP status reason phrase
     * @param message a human-readable error message
     * @param path the request path that caused the error
     */
    public ErrorResponse(LocalDateTime timestamp, int status, String error,
                         String message, String path) {
        this(timestamp, status, error, message, path, null);
    }

    /**
     * Represents a single field-level validation error.
     *
     * @param field the field name that failed validation
     * @param message the validation failure message
     */
    @Schema(description = "Field-level validation error")
    public record FieldError(
            @Schema(description = "Field name") String field,
            @Schema(description = "Validation error message") String message
    ) {}
}
