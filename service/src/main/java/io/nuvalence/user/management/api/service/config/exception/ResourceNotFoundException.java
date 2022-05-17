package io.nuvalence.user.management.api.service.config.exception;

/**
 * Custom Resource Not Found Exception.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
