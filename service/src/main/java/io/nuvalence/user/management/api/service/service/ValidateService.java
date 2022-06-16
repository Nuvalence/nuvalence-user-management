package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.ValidatePermissionDTO;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Service for validation.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class ValidateService {
    private final UserRepository userRepository;
    private final CerbosClient client;

    /**
     * Validates the user with the provided userName has the provided role.
     *
     * @param userName is a user's userName
     * @param permission is the role being checked
     * @param resourceName The name of the resource
     * @return a status code and the validation result
     */

    public ResponseEntity<ValidatePermissionDTO> validateUserPermission(String userName, String permission,
                                                                        String resourceName) {
        ValidatePermissionDTO validateResult = new ValidatePermissionDTO();

        Optional<UserEntity> userEntity = userRepository.findUserEntityByDisplayName(userName);
        if (userEntity.isEmpty()) {
            throw new ResourceNotFoundException("User not found!");
        }

        Boolean userHasRole = client.check(resourceName, userEntity.get(), permission);
        validateResult.setAllow(userHasRole);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(validateResult);
    }
}
