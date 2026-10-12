package com.financetracker.exception;

/**
 * Thrown when an attempt is made to create a resource that already exists.
 */
public class DuplicateResourceException extends RuntimeException {

    /**
     * Constructs a DuplicateResourceException with the given message.
     *
     * @param message the detail message
     */
    public DuplicateResourceException(String message) {
        super(message);
    }
}
