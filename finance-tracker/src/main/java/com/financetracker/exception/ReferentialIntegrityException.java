package com.financetracker.exception;

/**
 * Thrown when a delete operation is blocked because other entities reference the target resource.
 */
public class ReferentialIntegrityException extends RuntimeException {

    /**
     * Constructs a ReferentialIntegrityException with the given message.
     *
     * @param message the detail message
     */
    public ReferentialIntegrityException(String message) {
        super(message);
    }
}
