package io.nuvalence.user.management.api.service.config.exception;

/**
 * Custom exception for business logic issues.
 */
public class BusinessLogicException extends RuntimeException {
    public BusinessLogicException(String message) {
        super(message);
    }
}
