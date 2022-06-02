package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.controllers.UserPreferenceTypeApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceTypeDTO;
import io.nuvalence.user.management.api.service.service.UserPreferenceTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;

/**
 * Controller for User Preference API.
 */

@Service
@RequiredArgsConstructor
public class UserPreferenceTypeApiDelegateImpl implements UserPreferenceTypeApiDelegate {

    private final UserPreferenceTypeService userPreferenceTypeService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return UserPreferenceTypeApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<List<UserPreferenceTypeDTO>> getUserPreferenceTypes() {
        return userPreferenceTypeService.getAllUserPreferenceTypes();
    }
}
