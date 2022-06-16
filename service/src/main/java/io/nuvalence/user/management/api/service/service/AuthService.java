package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.auth.JwtUtility;
import io.nuvalence.user.management.api.service.auth.UsernameAuthenticationToken;
import io.nuvalence.user.management.api.service.config.exception.TokenAuthorizationException;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.TokenRefreshPacket;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Simple Authentication Service.
 */
@Component
public class AuthService {

    private final UserRepository userRepository;
    private final JwtUtility jwtUtility;
    private final Logger log = LoggerFactory.getLogger(AuthService.class);

    /**
     * Auth service constructor.
     * @param userRepository user repo.
     * @param jwtUtility jwt utility class.
     */
    public AuthService(UserRepository userRepository,
                       JwtUtility jwtUtility) {
        this.userRepository = userRepository;
        this.jwtUtility = jwtUtility;
    }

    /**
     * Use refresh token to return new access token.
     * @param request current servlet request.
     * @return Response Entity with status code and tokens.
     */
    public ResponseEntity<TokenRefreshPacket> refreshToken(HttpServletRequest request) {

        String authorizationHeader = request.getHeader(AUTHORIZATION);
        TokenRefreshPacket refreshPacket;
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            try {
                String refreshToken = authorizationHeader.substring("Bearer ".length());
                log.debug("Refresh token is: {}", refreshToken);
                refreshPacket = jwtUtility.createAuthTokenPacket(refreshToken);
                if (refreshPacket == null) {
                    throw new TokenAuthorizationException("The provided refresh token is invalid");
                }
            } catch (IOException e) {
                log.error("Error logging in: {}", e.getMessage());
                throw new TokenAuthorizationException(e.getMessage());
            }
        } else {
            throw new TokenAuthorizationException("Incorrect authorization parameters.");
        }

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(refreshPacket);
    }

    /**
     * Simple method to retrieve an authenticated user off of the security context.
     * @return an authenticated user entity.
     */
    public UserEntity fetchAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication instanceof UsernameAuthenticationToken) {
            Optional<UserEntity> user = userRepository.findUserEntityByEmail((
                    (UsernameAuthenticationToken) authentication).getUserEmail());
            if (user.isEmpty()) {
                throw new UsernameNotFoundException("No user found by email: " + user);
            }
            return user.get();
        } else {
            throw new TokenAuthorizationException("Current authority is not valid.");
        }
    }
}