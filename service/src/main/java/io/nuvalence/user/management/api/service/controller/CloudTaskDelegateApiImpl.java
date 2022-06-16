package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.controllers.CloudTaskApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.generated.models.UserUpdateRequest;
import io.nuvalence.user.management.api.service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Controller for cloud function user actions.
 */

@Service
@RequiredArgsConstructor
public class CloudTaskDelegateApiImpl implements CloudTaskApiDelegate {

    private final UserService userService;

    @Override
    public ResponseEntity<Void> addUser(UserCreationRequest body) {
        return userService.createUser(body);
    }

    @Override
    public ResponseEntity<UserDTO> updateUserById(UUID id, UserUpdateRequest body) {
        return userService.updateUserById(id, body);
    }

    @Override
    public ResponseEntity<Void> deleteUserById(UUID id) {
        return userService.deleteUser(id);
    }
}