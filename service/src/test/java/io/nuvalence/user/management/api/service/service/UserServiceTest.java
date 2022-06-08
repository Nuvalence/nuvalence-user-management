package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.CustomFieldDataTypeEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldTypeEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserCustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.generated.models.UserRoleDTO;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserCustomFieldRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:ClassDataAbstractionCoupling"})
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private UserCustomFieldRepository userCustomFieldRepository;

    @Mock
    private CerbosClient client;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<UserEntity> userCaptor;

    @Captor
    private ArgumentCaptor<UserRoleEntity> userRoleCaptor;

    // create user tests.
    @Test
    public void createUser_creates_a_user() {
        UserCreationRequest userModel = createUserCreationRequest();
        RoleDTO role = createRoleDto();
        userModel.setInitialRoles(List.of(role));
        RoleEntity roleEntity = createRoleEntity();

        when(roleRepository.getById(roleEntity.getId())).thenReturn(roleEntity);

        ResponseEntity<Void> res = userService.createUser(userModel);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository).save(userCaptor.capture());
        UserEntity savedEntity = userCaptor.getValue();
        assertEquals(savedEntity.getExternalId(), userModel.getExternalId());
        assertEquals(savedEntity.getDisplayName(), userModel.getDisplayName());
        assertEquals(savedEntity.getEmail(), userModel.getEmail());
        verify(userRoleRepository).save(userRoleCaptor.capture());
        UserRoleEntity savedUserRole = userRoleCaptor.getValue();
        assertEquals(savedUserRole.getRole().getId(), role.getId());
    }

    @Test
    public void createUser_creates_a_user_with_null_external_id() {
        UserCreationRequest userModel = createUserCreationRequest();
        userModel.setExternalId(null);
        RoleDTO role = createRoleDto();
        userModel.setInitialRoles(List.of(role));
        RoleEntity roleEntity = createRoleEntity();

        when(roleRepository.getById(roleEntity.getId())).thenReturn(roleEntity);

        ResponseEntity<Void> res = userService.createUser(userModel);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository).save(userCaptor.capture());
        UserEntity savedEntity = userCaptor.getValue();
        assertNull(savedEntity.getExternalId());
        assertEquals(savedEntity.getDisplayName(), userModel.getDisplayName());
        assertEquals(savedEntity.getEmail(), userModel.getEmail());
        verify(userRoleRepository).save(userRoleCaptor.capture());
        UserRoleEntity savedUserRole = userRoleCaptor.getValue();
        assertEquals(savedUserRole.getRole().getId(), role.getId());
    }

    @Test
    public void createUser_fails_ifEmailIsTaken() {
        UserCreationRequest userModel = createUserCreationRequest();
        UserEntity userEntity = createUserEntity();

        when(userRepository.findUserEntityByEmail(userModel.getEmail())).thenReturn(Optional.of(userEntity));

        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            userService.createUser(userModel);
        });
        assertTrue(exception.getMessage().contains("This Email is already assigned to a user."));
    }

    @Test
    public void createUser_fails_ifPassedANonExistentRole() {
        UserCreationRequest userModel = createUserCreationRequest();
        RoleDTO role = createRoleDto();
        userModel.setInitialRoles(List.of(role));

        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            userService.createUser(userModel);
        });
        assertTrue(exception.getMessage().contains("No role found for ROLE_TO_TEST."));
    }

    // Delete User Tests.
    @Test
    public void deleteUser_deletesUserCorrectly() {
        List<UserRoleEntity> userRoleEntities = List.of(createUserRoleEntity());
        UserEntity userEntity = createUserEntity();
        UserCustomFieldEntity userCustomFieldEntity = createUserCustomFieldEntity();
        userCustomFieldEntity.setUser(userEntity);
        List<UserCustomFieldEntity> userCustomFieldEntities = List.of(userCustomFieldEntity);

        when(userRoleRepository.findAllByUserId(userEntity.getId())).thenReturn(userRoleEntities);
        when(userCustomFieldRepository.findAllByUserId(userEntity.getId())).thenReturn(userCustomFieldEntities);
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));

        ResponseEntity<Void> res = userService.deleteUser(userEntity.getId());
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository).delete(userEntity);
    }

    @Test
    public void deleteUser_fails_ifUserDoesNotExist() {
        UserEntity userEntity = createUserEntity();

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.deleteUser(userEntity.getId());
        });
        assertEquals(exception.getMessage(), "User not found.");
    }


    // Assign Role to User Tests.
    @Test
    public void assignRoleToUser_assignsRoleIfValid() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());

        when(userRepository.findById(userEntity.get().getId())).thenReturn(userEntity);
        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(roleEntity);

        ResponseEntity<Void> res = userService.assignRoleToUser(userRole);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRoleRepository).save(userRoleCaptor.capture());
        UserRoleEntity capturedUserRole = userRoleCaptor.getValue();
        assertEquals(capturedUserRole.getRole(), roleEntity.get());
        assertEquals(capturedUserRole.getUser(), userEntity.get());
    }

    @Test
    public void assignRoleToUser_fails_ifUserDoesNotExist() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());

        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(roleEntity);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.assignRoleToUser(userRole);
        });
        assertTrue(exception.getMessage().contains("User or Role not found."));
    }

    @Test
    public void assignRoleToUser_fails_ifRoleDoesNotExist() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());

        when(userRepository.findById(userEntity.get().getId())).thenReturn(userEntity);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.assignRoleToUser(userRole);
        });
        assertTrue(exception.getMessage().contains("User or Role not found."));
    }

    // Remove Roll from User Tests
    @Test
    public void removeRoleToUser_assignsRoleIfValid() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());
        UserRoleEntity userRoleEntity = createUserRoleEntity();

        when(userRepository.findById(userEntity.get().getId())).thenReturn(userEntity);
        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(roleEntity);
        when(userRoleRepository.findByUserAndRole(userEntity.get(),
                roleEntity.get())).thenReturn(userRoleEntity);

        ResponseEntity<Void> res = userService.removeRoleFormUser(userRole);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRoleRepository).delete(userRoleEntity);
    }

    @Test
    public void removeRoleToUser_fails_ifUserDoesNotExist() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());

        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(roleEntity);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.removeRoleFormUser(userRole);
        });
        assertTrue(exception.getMessage().contains("User or Role not found."));
    }

    @Test
    public void removeRoleToUser_fails_ifRoleDoesNotExist() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());

        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(roleEntity);

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.removeRoleFormUser(userRole);
        });
        assertTrue(exception.getMessage().contains("User or Role not found."));
    }

    @Test
    public void removeRoleToUser_fails_ifRoleIsNotAssignedToUser() {
        UserRoleDTO userRole = createUserRoleDto();
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        Optional<RoleEntity> roleEntity = Optional.of(createRoleEntity());

        when(userRepository.findById(userEntity.get().getId())).thenReturn(userEntity);
        when(roleRepository.findById(roleEntity.get().getId())).thenReturn(roleEntity);

        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            userService.removeRoleFormUser(userRole);
        });
        assertTrue(exception.getMessage().contains("The role requested does not exist on John Locke."));
    }

    // Fetch Roles by UserId Tests
    @Test
    public void fetchRolesByUserId_assignsRoleIfValid() {
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        UserRoleEntity userRoleEntity = createUserRoleEntity();
        userEntity.get().setUserRoleEntities(List.of(userRoleEntity));

        when(userRepository.findById(userEntity.get().getId())).thenReturn(userEntity);
        when(client.getRolePermissionMappings(ArgumentMatchers.anyString())).thenReturn(Collections.emptyMap());

        ResponseEntity<List<RoleDTO>> res = userService.fetchRolesByUserId(userEntity.get().getId(),
                "default_resource");
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody(), List.of(createRoleDto()));
    }

    @Test
    public void fetchRolesByUserId_fails_ifUserDoesNotExist() {
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.fetchRolesByUserId(userEntity.get().getId(), "default_resource");
        });
        assertEquals(exception.getMessage(), "User not found!");
    }

    // Fetch Users Tests
    @Test
    public void getUserList_returnsUserListIfValid() {
        List<UserEntity> userEntities = List.of(createUserEntity());

        when(userRepository.findAll()).thenReturn(userEntities);
        when(client.getRolePermissionMappings(ArgumentMatchers.anyString())).thenReturn(Collections.emptyMap());
        ResponseEntity<List<UserDTO>> res = userService.getUserList("default_resource");
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody(), List.of(createUserDto()));
    }

    @Test
    public void getUserList_fails_ifNoUsersExist() {
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserList("default_resource");
        });
        assertEquals(exception.getMessage(), "No users found.");
    }

    // Fetch User by Id Tests.
    @Test
    public void fetchUserById_returnsUserIfValid() {
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        userEntity.get().setUserRoleEntities(List.of(createUserRoleEntity()));
        UserDTO user = createUserDto();
        user.setAssignedRoles(List.of(createRoleDto()));

        when(userRepository.findById(userEntity.get().getId())).thenReturn(userEntity);
        when(client.getRolePermissionMappings(ArgumentMatchers.anyString())).thenReturn(Collections.emptyMap());

        ResponseEntity<UserDTO> res = userService.fetchUserById(userEntity.get().getId(), "default_resource");
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody(), user);
    }

    @Test
    public void fetchUserById_fails_ifNoUserExists() {
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.fetchUserById(userEntity.get().getId(), "default_resource");
        });
        assertEquals(exception.getMessage(), "User not found!");
    }

    @Test
    public void fetchUserByEmail_returnsUserIfValid() {
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());
        userEntity.get().setUserRoleEntities(List.of(createUserRoleEntity()));
        UserDTO user = createUserDto();
        user.setAssignedRoles(List.of(createRoleDto()));

        when(userRepository.findUserEntityByEmail(userEntity.get().getEmail())).thenReturn(userEntity);

        ResponseEntity<UserDTO> res = userService.fetchUserByEmail(userEntity.get().getEmail(), "default_resource");
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody(), user);
    }

    @Test
    public void fetchUserByEmail_fails_ifNoUserExists() {
        Optional<UserEntity> userEntity = Optional.of(createUserEntity());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.fetchUserByEmail(userEntity.get().getEmail(), "default_resource");
        });
        assertEquals(exception.getMessage(), "User not found!");
    }

    // Helper methods

    private UserCreationRequest createUserCreationRequest() {
        UserCreationRequest userModel = new UserCreationRequest();
        userModel.setDisplayName("John Locke");
        userModel.setEmail("Skipper@theIsland.com");
        userModel.setExternalId("TestExternalId1234");
        return userModel;
    }

    private RoleDTO createRoleDto() {
        RoleDTO role = new RoleDTO();
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        role.setRoleName("ROLE_TO_TEST");
        role.setPermissions(Collections.emptyList());
        return role;
    }

    private RoleEntity createRoleEntity() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setRoleName("ROLE_TO_TEST");
        roleEntity.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        return roleEntity;
    }

    private UserEntity createUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("Skipper@theIsland.com");
        userEntity.setDisplayName("John Locke");
        userEntity.setExternalId("TestExternalId1234");
        return userEntity;
    }

    private UserCustomFieldEntity createUserCustomFieldEntity() {
        CustomFieldTypeEntity customFieldTypeEntity = new CustomFieldTypeEntity();
        customFieldTypeEntity.setId(UUID.fromString("a80a48b5-2995-4c54-9bd5-ebc258fab4ba"));
        customFieldTypeEntity.setType("drop_down_list");

        CustomFieldDataTypeEntity dataType = new CustomFieldDataTypeEntity();
        dataType.setId(UUID.fromString("3e724ddf-4d09-452b-ae98-a8e3a76af19c"));
        dataType.setType("string");

        CustomFieldEntity customFieldEntity = new CustomFieldEntity();
        customFieldEntity.setId(UUID.randomUUID());
        customFieldEntity.setName("CUSTOM_FIELD_1");
        customFieldEntity.setType(customFieldTypeEntity);
        customFieldEntity.setDataType(dataType);
        customFieldEntity.setDisplayText("Custom Field 1");

        UserCustomFieldEntity userCustomFieldEntity = new UserCustomFieldEntity();
        userCustomFieldEntity.setId(UUID.randomUUID());
        userCustomFieldEntity.setCustomField(customFieldEntity);
        userCustomFieldEntity.setCustomFieldValueString("TEST1");

        return userCustomFieldEntity;
    }

    private UserRoleEntity createUserRoleEntity() {
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setRole(createRoleEntity());
        userRoleEntity.setUser(createUserEntity());
        return userRoleEntity;
    }

    private UserDTO createUserDto() {
        UserEntity userEntity = createUserEntity();
        UserDTO user = new UserDTO();
        user.setExternalId(userEntity.getExternalId());
        user.setEmail(userEntity.getEmail());
        user.setDisplayName(userEntity.getDisplayName());
        return user;
    }

    private UserRoleDTO createUserRoleDto() {
        UserRoleDTO userRoleDto = new UserRoleDTO();
        userRoleDto.setRoleId(createRoleDto().getId());
        userRoleDto.setUserId(createUserEntity().getId());
        return userRoleDto;
    }

}
