package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.controllers.ValidateApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.ValidatePermissionDTO;
import io.nuvalence.user.management.api.service.service.ValidateService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Optional;

/**
 * Controller for Permission API.
 */

@Service
@RequiredArgsConstructor
public class ValidateApiDelegateImpl implements ValidateApiDelegate {

    private final ValidateService validateService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return ValidateApiDelegate.super.getRequest();
    }

    public ResponseEntity<ValidatePermissionDTO> validateUserPermission(String userName, String role, String resource) {
        return validateService.validateUserPermission(userName, role, resource);
    }
}
