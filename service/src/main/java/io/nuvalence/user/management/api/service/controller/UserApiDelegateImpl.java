package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.controllers.UserApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.ApplicationPreferenceDTO;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateUserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.generated.models.UserRoleDTO;
import io.nuvalence.user.management.api.service.mapper.UserPreferenceEntityMapper;
import io.nuvalence.user.management.api.service.service.UserPreferenceService;
import io.nuvalence.user.management.api.service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
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
    public ResponseEntity<UserPreferenceDTO> getPreferencesById(UUID id) {
        UserPreferenceEntity preferences = userPreferenceService.getPreferencesByUserId(id);

        UserPreferenceDTO preferencesDto = UserPreferenceEntityMapper.INSTANCE
                .userPreferencesEntityToDto(preferences);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(preferencesDto);
    }

    @Override
    public ResponseEntity<UserPreferenceDTO> getSupportedPreferencesById(UUID id, UUID appId) {
        UserPreferenceEntity preferences = userPreferenceService.getSupportedPreferencesByUserId(id, appId);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(UserPreferenceEntityMapper.INSTANCE.userPreferencesEntityToDto(preferences));
    }

    @Override
    public ResponseEntity<Void> updatePreferences(UUID id,
                                                  UserPreferenceDTO userPreferences) {
        userPreferenceService.updatePreferencesByUserId(id, userPreferences);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updateApplicationPreferences(UUID id,
                                                             UUID appId,
                                                             ApplicationPreferenceDTO userPreferences) {
        userPreferenceService.updateApplicationPreferencesById(id, appId, userPreferences);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> updateUserCustomFieldValue(UUID id, CreateOrUpdateUserCustomFieldDTO customField) {
        return userService.updateCustomField(id, customField);
    }

}
