package com.financetracker.exception;

/**
 * Thrown when a user attempts an operation they are not permitted to perform.
 */
public class ForbiddenOperationException extends RuntimeException {

    /**
     * Constructs a ForbiddenOperationException with the given message.
     *
     * @param message the detail message
     */
    public ForbiddenOperationException(String message) {
        super(message);
    }
}
