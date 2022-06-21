package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserRoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private CerbosClient client;

    @InjectMocks
    private RoleService roleService;

    @Captor
    private ArgumentCaptor<RoleEntity> roleCaptor;

    // Add Role.
    @Test
    public void addRole_addsRoleIfValid() {
        RoleDTO role = createRoleDto();

        ResponseEntity<Void> res = roleService.addRole(role);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(roleRepository).save(roleCaptor.capture());
        RoleEntity roleCaptured = roleCaptor.getValue();
        assertEquals(roleCaptured.getId(), role.getId());
        assertEquals(roleCaptured.getRoleName(), role.getRoleName());
        assertEquals(roleCaptured.getDisplayName(), role.getDisplayName());
    }

    @Test
    public void addRole_fails_ifRoleExists() {
        RoleDTO role = createRoleDto();

        when(roleRepository.findByRoleName(role.getRoleName())).thenReturn(new RoleEntity());

        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            roleService.addRole(role);
        });
        assertEquals(exception.getMessage(), "This role already exists.");
    }

    // Update role
    @Test
    public void updateRole_updatesRoleIfValid() {
        RoleDTO role = createRoleDto();
        when(client.updateRolePermissionMappings(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                any()))
                .thenReturn(true);
        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(createRoleEntity()));

        ResponseEntity<Void> response = roleService.updateRole(role.getId(), role, "default_resource");
        assertEquals(response.getStatusCode(), HttpStatus.OK);
    }

    @Test
    public void updateRole_fails_ifInvalidRoleId() {
        RoleDTO role = createRoleDto();

        when(roleRepository.findById(role.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            roleService.updateRole(role.getId(), role, "default_resource");
        });
        assertEquals(exception.getMessage(), "This role does not exist.");
    }

    @Test
    public void updateRole_fails_ifInvalidPermission() {
        RoleDTO role = createRoleDto();
        role.setPermissions(Arrays.asList("permissionToFail"));

        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(createRoleEntity()));

        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            roleService.updateRole(role.getId(), role, "default_resource");
        });
        assertEquals(exception.getMessage(), "The permission(s) are invalid.");
    }

    // Get All Roles.

    @Test
    public void getAllRoles_returnsAllRoles() {
        RoleEntity role = createRoleEntity();

        when(roleRepository.findAll()).thenReturn(List.of(role));

        ResponseEntity<List<RoleDTO>> res = roleService.getAllRoles();
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        RoleDTO roleDto = createRoleDto();
        roleDto.setPermissions(null);
        assertEquals(res.getBody(), List.of(roleDto));
    }

    @Test
    public void getAllRolesByResource_returnsAllRoles() {
        RoleEntity role = createRoleEntity();

        when(roleRepository.findAll()).thenReturn(List.of(role));
        when(client.getRolePermissionMappings(ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyMap());

        ResponseEntity<List<RoleDTO>> res = roleService.getAllRolesByResource("default_resource");
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        RoleDTO roleDto = createRoleDto();
        roleDto.setPermissions(Collections.emptyList());
        assertEquals(res.getBody(), List.of(roleDto));
    }

    // Delete Role.

    @Test
    public void deleteRoleById_deletesRoleIfValid() {
        RoleEntity role = createRoleEntity();

        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
        when(client.removeRole(ArgumentMatchers.anyString(), ArgumentMatchers.anyString()))
                .thenReturn(true);

        ResponseEntity<Void> res = roleService.deleteRoleById(role.getId(), "default_resource");
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(roleRepository).delete(role);
    }

    @Test
    public void deleteRoleById_fails_ifThatRoleDoesNotExist() {
        RoleEntity role = createRoleEntity();

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            roleService.deleteRoleById(role.getId(), "default_resource");
        });
        assertEquals(exception.getMessage(), "There is no role that exists with this id.");
    }

    // Get Users by Role id.
    @Test
    public void getUsersByRoleId_returnsUsersIfValid() {
        RoleEntity role = createRoleEntity();
        List<UserRoleEntity> userRoleEntities = generateUserRoleList();

        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
        when(userRoleRepository.findAllByRoleId(role.getId())).thenReturn(userRoleEntities);
        when(client.getRolePermissionMappings(ArgumentMatchers.anyString()))
                .thenReturn(Collections.emptyMap());

        ResponseEntity<List<UserDTO>> res = roleService.getUsersByRoleId(role.getId(), "default_resource");
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody().size(), 2);

        // Dynamic check both array lists with one loop
        for (int i = 0; i < res.getBody().size(); i++) {
            UserDTO userRes = res.getBody().get(i);
            UserEntity userReq = userRoleEntities.get(i).getUser();
            assertEquals(userRes.getId(), userReq.getId());
            assertEquals(userRes.getDisplayName(), userReq.getDisplayName());
            assertEquals(userRes.getEmail(), userReq.getEmail());
        }
    }

    @Test
    public void getUsersByRoleId_fails_ifTheRoleDoesNotExist() {
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            roleService.getUsersByRoleId(createRoleEntity().getId(), "default_resource");
        });
        assertEquals(exception.getMessage(), "There is no role with this id");
    }

    @Test
    public void getUsersByRoleId_fails_ifNoUserHasThisRole() {
        RoleEntity role = createRoleEntity();

        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));

        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            roleService.getUsersByRoleId(role.getId(), "default_resource");
        });
        assertEquals(exception.getMessage(), "There are no users with this role");
    }

    // Helpers

    private RoleDTO createRoleDto() {
        RoleDTO role = new RoleDTO();
        role.setRoleName("ROLE_TO_TEST");
        role.setDisplayName("Role To Test");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        role.setPermissions(Arrays.asList("permissionToTest"));
        return role;
    }

    private RoleEntity createRoleEntity() {
        RoleEntity role = new RoleEntity();
        role.setRoleName("ROLE_TO_TEST");
        role.setDisplayName("Role To Test");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        return role;
    }

    private List<UserRoleEntity> generateUserRoleList() {
        UserEntity user0 = new UserEntity();
        user0.setId(UUID.fromString("ca8cfd1b-8643-4185-ba7f-8c8fbc9a7da6"));
        user0.setEmail("MrIncognito@Illuminati.org");
        user0.setDisplayName("Rodney Safe-Valley");
        UserEntity user1 = new UserEntity();
        user1.setId(UUID.fromString("ca8cfd1b-1337-4185-ba7f-8c8fbc9a7da6"));
        user1.setEmail("iUseArchBtw@thunderbird.com");
        user1.setDisplayName("It doesn't compile");
        UserRoleEntity userRoleEntity = generateUserRoleEntity(user0);
        UserRoleEntity userRoleEntity1 = generateUserRoleEntity(user1);

        return List.of(userRoleEntity, userRoleEntity1);
    }

    private UserRoleEntity generateUserRoleEntity(UserEntity user) {
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUser(user);
        userRoleEntity.setRole(createRoleEntity());
        return userRoleEntity;
    }
}
