package io.nuvalence.user.management.api.service.auth.filters;

import io.nuvalence.user.management.api.service.auth.JwtUtility;
import io.nuvalence.user.management.api.service.auth.UsernameAuthenticationToken;
import io.nuvalence.user.management.api.service.config.exception.TokenAuthorizationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

/**
 * Custom Authorization Filter.
 */
public class CustomAuthorizationFilter extends OncePerRequestFilter {

    private final Logger log = LoggerFactory.getLogger(CustomAuthorizationFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // If logging in or token refresh, then ignore authorization.
        if (request.getServletPath().equals("/api/v2/auth/token/refresh")
                || request.getServletPath().equals("/")
                || request.getServletPath().equals("/api/v2/cloud-task/user")) {
            filterChain.doFilter(request, response);
        } else {
            String authorizationHeader = request.getHeader(AUTHORIZATION);
            if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
                try {
                    String token = authorizationHeader.substring("Bearer ".length());

                    // Verify JWT and create a security context token.
                    UsernameAuthenticationToken authenticationToken =
                            JwtUtility.verifyAccessTokenForSecurityContext(token);

                    /*
                     * Allows us to ensure the user has provided valid JWT to pass the auth check,
                     * additionally allows us to grab user entity from the security context in memory.
                     */
                    if (SecurityContextHolder.getContext().getAuthentication() == null) {
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);
                    }
                    filterChain.doFilter(request, response);

                } catch (Exception e) {
                    log.error("Error authorizing request: {}", e.getMessage());
                    // Remove security context if user provides invalid credentials.
                    if (SecurityContextHolder.getContext().getAuthentication() != null) {
                        SecurityContextHolder.getContext().setAuthentication(null);
                    }
                    throw new TokenAuthorizationException(e.getMessage());
                }
            } else {
                filterChain.doFilter(request, response);
            }

        }
    }
}
