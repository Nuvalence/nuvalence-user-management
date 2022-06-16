package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.controllers.AuthApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.TokenRefreshPacket;
import io.nuvalence.user.management.api.service.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;

/**
 * Controller for Authentication.
 */
@Service
public class AuthApiDelegateImpl implements AuthApiDelegate {

    private final AuthService authService;
    private final HttpServletRequest request;

    /**
     * Constructor for Auth API.
     * @param authService auth service.
     * @param httpServletRequest http request.
     */
    @Autowired
    public AuthApiDelegateImpl(AuthService authService,
                               HttpServletRequest httpServletRequest) {
        this.authService = authService;
        this.request = httpServletRequest;
    }

    @Override
    public ResponseEntity<TokenRefreshPacket> refreshToken() {
        return authService.refreshToken(request);
    }
}