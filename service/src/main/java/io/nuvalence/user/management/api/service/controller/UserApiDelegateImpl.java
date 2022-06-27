package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.controllers.UserApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateUserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.generated.models.UserRoleDTO;
import io.nuvalence.user.management.api.service.service.UserPreferenceService;
import io.nuvalence.user.management.api.service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for User API.
 */

@Service
@RequiredArgsConstructor
public class UserApiDelegateImpl implements UserApiDelegate {

    private final UserService userService;
    private final UserPreferenceService userPreferenceService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return UserApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<UserDTO> getUserById(UUID id, String resource) {
        return userService.fetchUserById(id, resource);
    }

    @Override
    public ResponseEntity<UserDTO> getUserByEmail(String email, String resource) {
        return userService.fetchUserByEmail(email, resource);
    }

    @Override
    public ResponseEntity<List<UserDTO>> getUserList(String resource) {
        return userService.getUserList(resource);
    }

    @Override
    public ResponseEntity<List<RoleDTO>> getUserRolesById(UUID id, String resource) {
        return userService.fetchRolesByUserId(id, resource);
    }

    @Override
    public ResponseEntity<Void> assignRoleToUser(UserRoleDTO userRole) {
        return userService.assignRoleToUser(userRole);
    }

    @Override
    public ResponseEntity<Void> removeRoleFromUser(UserRoleDTO userRole) {
        return userService.removeRoleFormUser(userRole);
    }

    @Override
    public ResponseEntity<UserPreferenceDTO> getUserPreferences(UUID id) {
        UserPreferenceDTO preferences = userPreferenceService.getUserPreferences(id, null);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(preferences);
    }

    @Override
    public ResponseEntity<UserPreferenceDTO> getUserApplicationPreferences(UUID id, UUID appId) {
        UserPreferenceDTO preferences = userPreferenceService.getUserPreferences(id, appId);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(preferences);
    }

    @Override
    public ResponseEntity<Void> updatePreferences(UUID id,
                                                  Map<String, String> userPreferences) {
        userPreferenceService.updateUserPreferences(id, userPreferences);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updateApplicationPreferences(UUID id,
                                                             UUID appId,
                                                             Map<String, String> userPreferences) {
        userPreferenceService.updateUserApplicationPreferences(id, appId, userPreferences);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updateUserCustomFieldValue(UUID id, CreateOrUpdateUserCustomFieldDTO customField) {
        return userService.updateCustomField(id, customField);
    }

}
