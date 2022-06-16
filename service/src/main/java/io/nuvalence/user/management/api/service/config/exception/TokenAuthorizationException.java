package io.nuvalence.user.management.api.service.config.exception;

/**
 * Custom exception for token authorization issues.
 */
public class TokenAuthorizationException extends RuntimeException  {
    public TokenAuthorizationException(String message) {
        super(message);
    }
}