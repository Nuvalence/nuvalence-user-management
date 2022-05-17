package io.nuvalence.user.management.api.service.config.exception;

/**
 * Custom InternalServer Exception class.
 */
public class InternalServerException extends RuntimeException {
    public InternalServerException(String message) {
        super(message);
    }

    public InternalServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
