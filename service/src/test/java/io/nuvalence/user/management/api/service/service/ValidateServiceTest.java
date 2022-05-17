package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.cerbos.models.CheckResourceSetResponse;
import io.nuvalence.user.management.api.service.cerbos.models.CheckResourceSetResponseActionEffectMap;
import io.nuvalence.user.management.api.service.cerbos.models.Effect;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import io.nuvalence.user.management.api.service.generated.models.ValidatePermissionDTO;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ValidateServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CerbosClient client;

    @InjectMocks
    private ValidateService validateService;

    @Test
    public void validateUserPermission_returnsAllowTrue() {
        when(userRepository.findUserEntityByDisplayName("allow")).thenReturn(Optional.of(getUserEntity()));
        when(client.check(ArgumentMatchers.anyString(), ArgumentMatchers.<UserEntity>any(), any()))
                .thenReturn(true);

        ResponseEntity<ValidatePermissionDTO> response = validateService
                .validateUserPermission("allow", "permissionToTest", "default_resource");
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().getAllow(), true);
    }

    @Test
    public void validateUserPermission_returnsAllowFalse() {
        when(userRepository.findUserEntityByDisplayName("allow")).thenReturn(Optional.of(getUserEntity()));
        when(client.check(ArgumentMatchers.anyString(), ArgumentMatchers.<UserEntity>any(), any()))
                .thenReturn(false);

        ResponseEntity<ValidatePermissionDTO> response = validateService
                .validateUserPermission("allow", "permissionToTest", "default_resource");
        assertEquals(response.getStatusCode(), HttpStatus.OK);
        assertEquals(response.getBody().getAllow(), false);
    }

    private UserEntity getUserEntity() {
        var userEntity = new UserEntity();
        UserRoleEntity userRole = new UserRoleEntity();
        RoleEntity role = new RoleEntity();
        role.setRoleName("allow");
        userRole.setRole(role);
        userEntity.setDisplayName("allow");
        userEntity.setUserRoleEntities(Arrays.asList(userRole));
        return userEntity;
    }

    private CheckResourceSetResponse getCheckResponse(Boolean allow) {
        return CheckResourceSetResponse.builder()
                .resourceInstances(Map.of("allow", CheckResourceSetResponseActionEffectMap.builder()
                        .actions(Map.of("permissionToTest",
                                allow ? Effect.EFFECT_ALLOW : Effect.EFFECT_DENY))
                        .build()))
                .build();
    }
}
