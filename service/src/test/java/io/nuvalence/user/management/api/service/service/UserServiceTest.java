package io.nuvalence.user.management.api.service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import io.nuvalence.user.management.api.service.enums.CustomFieldDataType;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateUserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.generated.models.UserRoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserUpdateRequest;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.repository.CustomFieldRepository;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserCustomFieldRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import io.nuvalence.user.management.api.service.repository.UserRoleRepository;
import org.assertj.core.util.IterableUtil;
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

import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

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
    private CustomFieldRepository customFieldRepository;

    @Mock
    private CerbosClient client;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<UserEntity> userCaptor;

    @Captor
    private ArgumentCaptor<UserCustomFieldEntity> userCustomFieldCaptor;

    @Captor
    private ArgumentCaptor<Iterable<UserCustomFieldEntity>> userCustomFieldListCaptor;

    @Captor
    private ArgumentCaptor<Iterable<UserRoleEntity>> userRoleListCaptor;

    @Captor
    private ArgumentCaptor<UserRoleEntity> userRoleCaptor;

    // create user tests.
    @Test
    public void createUser_creates_a_user() {
        UserCreationRequest userModel = createUserCreationRequest();
        RoleDTO role = createRoleDto();
        userModel.setInitialRoles(List.of(role));
        RoleEntity roleEntity = createRoleEntity();
        List<UserCustomFieldEntity> userCustomFieldEntities = createUserCustomFieldEntityList();
        List<CreateOrUpdateUserCustomFieldDTO> customFields = getCreateUserCustomFieldList(userCustomFieldEntities);
        userModel.setCustomFields(customFields);
        List<CustomFieldEntity> customFieldList = userCustomFieldEntities
                .stream().map(UserCustomFieldEntity::getCustomField)
                .collect(Collectors.toList());
        when(roleRepository.findAllById(any())).thenReturn(List.of(roleEntity));
        when(customFieldRepository.findAllById(any())).thenReturn(customFieldList);

        ResponseEntity<Void> res = userService.createUser(userModel);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository).save(userCaptor.capture());
        UserEntity savedEntity = userCaptor.getValue();
        assertEquals(savedEntity.getExternalId(), userModel.getExternalId());
        assertEquals(savedEntity.getDisplayName(), userModel.getDisplayName());
        assertEquals(savedEntity.getEmail(), userModel.getEmail());

        verify(userRoleRepository).saveAll(userRoleListCaptor.capture());
        Iterable<UserRoleEntity> savedUserRoles = userRoleListCaptor.getValue();
        assertEquals(savedUserRoles.iterator().next().getRole().getId(), role.getId());

        verify(userCustomFieldRepository).saveAll(userCustomFieldListCaptor.capture());
        Iterable<UserCustomFieldEntity> savedUserCustomFields = userCustomFieldListCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(savedUserCustomFields), userModel.getCustomFields().size());
    }

    @Test
    public void createUser_throwsExceptionIf_null_external_id() {
        UserCreationRequest userModel = createUserCreationRequest();
        userModel.setExternalId(null);
        RoleDTO role = createRoleDto();
        userModel.setInitialRoles(List.of(role));


        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            userService.createUser(userModel);
        });
        assertEquals(exception.getMessage(), "Missing identifier for user: Skipper@theIsland.com");
    }

    @Test
    public void createUser_throwsExceptionIf_empty_external_id() {
        UserCreationRequest userModel = createUserCreationRequest();
        userModel.setExternalId("");
        RoleDTO role = createRoleDto();
        userModel.setInitialRoles(List.of(role));


        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            userService.createUser(userModel);
        });
        assertEquals(exception.getMessage(), "Missing identifier for user: Skipper@theIsland.com");
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

    @Test
    public void createUser_fails_ifPassedANonExistentCustomField() {
        UserCreationRequest userModel = createUserCreationRequest();
        CreateOrUpdateUserCustomFieldDTO customField = new CreateOrUpdateUserCustomFieldDTO();
        customField.setCustomFieldId(UUID.randomUUID());
        customField.setValue("FIELD_1");
        userModel.setCustomFields(List.of(customField));
        when(userRepository.findUserEntityByEmail(any())).thenReturn(Optional.empty());
        when(userRepository.findUserEntityByExternalId(any())).thenReturn(Optional.empty());
        when(customFieldRepository.findAllById(any())).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(BusinessLogicException.class, () -> {
            userService.createUser(userModel);
        });
        assertTrue(exception.getMessage().contains("No custom field found with id: "
                + customField.getCustomFieldId().toString()));
    }

    // Update user tests.
    @Test
    public void updateUser_fully_updates_a_user() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findById(originalUserEntity.getId())).thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the new values, update request, and expected new entity
        final String newName = "John Locke II";
        final String newEmail = "Skipper@theisland.net";
        final String newExternalId = "TestExternalId123456789";
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setDisplayName(newName);
        updateRequest.setEmail(newEmail);
        updateRequest.setExternalId(newExternalId);
        UserEntity newUserEntity = createUserEntity();
        newUserEntity.setDisplayName(newName);
        newUserEntity.setEmail(newEmail);
        newUserEntity.setExternalId(newExternalId);

        // Fake the UserEntity return when save is called in the userService.updateUser(...) function
        when(userRepository.save(isA(UserEntity.class))).thenReturn(newUserEntity);

        // Make the update request
        ResponseEntity<UserDTO> res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository).save(userCaptor.capture()); // userRepository should have been asked to save once
        UserEntity savedEntity = userCaptor.getValue(); // Capture what it was sent to save

        // Validate that the UserEntity submitted to userRepository.save and the returned UserDTO are correct
        validateUserUpdate(originalUserEntity, updateRequest, savedEntity, res.getBody());
    }

    @Test
    public void updateUser_partially_updates_a_user() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findById(originalUserEntity.getId())).thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the new values and utility objects
        final String newName = "John Locke II";
        final String newEmail = "Skipper@theisland.net";
        final String newExternalId = "TestExternalId123456789";
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        UserEntity expectedUserEntity = createUserEntity();

        // Test changing only the user's display name
        updateRequest.setDisplayName(newName);
        expectedUserEntity.setDisplayName(newName);
        when(userRepository.save(isA(UserEntity.class))).thenReturn(expectedUserEntity);
        ResponseEntity<UserDTO> res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository, times(1)).save(userCaptor.capture());
        UserEntity savedEntity = userCaptor.getValue();
        validateUserUpdate(originalUserEntity, updateRequest, savedEntity, res.getBody());
        expectedUserEntity.setDisplayName(originalUserEntity.getDisplayName()); // Reset for next test

        // Test changing only the user's email
        updateRequest = new UserUpdateRequest(); // Clear out old changes requested
        updateRequest.setEmail(newEmail);
        expectedUserEntity.setEmail(newEmail); // User repo return is automatically updated via reference
        res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository, times(2)).save(userCaptor.capture()); // 2x now
        savedEntity = userCaptor.getValue();
        validateUserUpdate(originalUserEntity, updateRequest, savedEntity, res.getBody());
        expectedUserEntity.setEmail(originalUserEntity.getEmail()); // Reset for next test

        // Test changing only the user's external id
        updateRequest = new UserUpdateRequest(); // Clear out old changes requested
        updateRequest.setExternalId(newExternalId);
        expectedUserEntity.setExternalId(newExternalId); // User repo return is automatically updated via reference
        res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository, times(3)).save(userCaptor.capture()); // 3x now
        savedEntity = userCaptor.getValue();
        validateUserUpdate(originalUserEntity, updateRequest, savedEntity, res.getBody());

        // Test submitting the existing email and external id, but a changed display name
        updateRequest = new UserUpdateRequest(); // Clear out old changes requested
        updateRequest.setDisplayName(newName);
        updateRequest.setEmail(originalUserEntity.getEmail());
        updateRequest.setExternalId(originalUserEntity.getExternalId());
        expectedUserEntity.setDisplayName(newName); // User repo return is automatically updated via reference
        expectedUserEntity.setEmail(originalUserEntity.getEmail());
        expectedUserEntity.setExternalId(originalUserEntity.getExternalId());
        res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository, times(4)).save(userCaptor.capture()); // 4x now
        savedEntity = userCaptor.getValue();
        validateUserUpdate(originalUserEntity, updateRequest, savedEntity, res.getBody());
    }

    // A user should not be updated if any part of the request turns out to be invalid
    // As an example, if both email and externalId are sent, but externalId is already assigned, update nothing
    @Test
    public void updateUser_does_not_partially_update_on_validation_failure() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findById(originalUserEntity.getId())).thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the new values and utility objects
        final String newName = "John Locke II";
        final String newEmail = "Skipper@theisland.net";
        final String newExternalId = "TestExternalId123456789";
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setDisplayName(newName);
        updateRequest.setEmail(newEmail);
        updateRequest.setExternalId(newExternalId);
        Throwable exception;

        // Valid name and email, but invalid external ID
        when(userRepository.findUserEntityByExternalId(updateRequest.getExternalId()))
            .thenReturn(Optional.of(new UserEntity()));
        exception = assertThrows(BusinessLogicException.class,
            () -> userService.updateUserById(originalUserEntity.getId(), updateRequest));
        assertEquals("This ExternalId is already assigned to a user.", exception.getMessage());
        when(userRepository.findUserEntityByExternalId(updateRequest.getExternalId())).thenReturn(Optional.empty());

        // Valid name and external ID, but invalid email
        when(userRepository.findUserEntityByEmail(updateRequest.getEmail())).thenReturn(Optional.of(new UserEntity()));
        exception = assertThrows(BusinessLogicException.class,
            () -> userService.updateUserById(originalUserEntity.getId(), updateRequest));
        assertEquals("This Email is already assigned to a user.", exception.getMessage());

        // Make sure the user was never updated in the repository
        verify(userRepository, times(0)).save(any());
    }

    // Makes sure that the request still succeeds (and doesn't affect anything) if no data is sent to update
    @Test
    public void updateUser_does_not_update_a_user() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findById(originalUserEntity.getId())).thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the empty update request and expected new entity
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        UserEntity newUserEntity = createUserEntity();

        // Fake the UserEntity return when save is called in the userService.updateUser(...) function
        when(userRepository.save(isA(UserEntity.class))).thenReturn(newUserEntity);

        // Make the update request
        ResponseEntity<UserDTO> res = userService.updateUserById(originalUserEntity.getId(), updateRequest);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userRepository).save(userCaptor.capture()); // userRepository should have been asked to save once
        UserEntity savedEntity = userCaptor.getValue(); // Capture what it was sent to save

        // Validate that the UserEntity submitted to userRepository.save and the returned UserDTO are correct
        validateUserUpdate(originalUserEntity, updateRequest, savedEntity, res.getBody());
    }

    @Test
    public void updateUser_fails_ifUserDoesNotExist() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findById(originalUserEntity.getId())).thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the new values and utility objects
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        Throwable exception;

        // Valid name and email, but invalid external ID
        when(userRepository.findById(originalUserEntity.getId())).thenReturn(Optional.empty());
        exception = assertThrows(ResourceNotFoundException.class,
            () -> userService.updateUserById(originalUserEntity.getId(), updateRequest));
        assertEquals("User not found.", exception.getMessage());

        // Make sure the user was never updated in the repository
        verify(userRepository, times(0)).save(any());
    }

    @Test
    public void updateUser_fails_ifExternalIdAlreadyAssigned() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findById(originalUserEntity.getId())).thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the new values and utility objects
        final String newExternalId = "TestExternalId123456789";
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setExternalId(newExternalId);
        Throwable exception;

        // Run the invalid external ID test
        when(userRepository.findUserEntityByExternalId(updateRequest.getExternalId()))
            .thenReturn(Optional.of(new UserEntity()));
        exception = assertThrows(BusinessLogicException.class,
            () -> userService.updateUserById(originalUserEntity.getId(), updateRequest));
        assertEquals("This ExternalId is already assigned to a user.", exception.getMessage());

        // Make sure the user was never updated in the repository
        verify(userRepository, times(0)).save(any());
    }

    @Test
    public void updateUser_fails_ifEmailAlreadyAssigned() {
        // Create a user and make sure the user repository responds with it
        UserEntity originalUserEntity = createUserEntity();
        when(userRepository.findById(originalUserEntity.getId())).thenAnswer(x -> Optional.of(createUserEntity()));

        // Set up the new values and utility objects
        final String newEmail = "Skipper@theisland.net";
        UserUpdateRequest updateRequest = new UserUpdateRequest();
        updateRequest.setEmail(newEmail);
        Throwable exception;

        // Run the invalid email test
        when(userRepository.findUserEntityByEmail(updateRequest.getEmail())).thenReturn(Optional.of(new UserEntity()));
        exception = assertThrows(BusinessLogicException.class,
            () -> userService.updateUserById(originalUserEntity.getId(), updateRequest));
        assertEquals("This Email is already assigned to a user.", exception.getMessage());

        // Make sure the user was never updated in the repository
        verify(userRepository, times(0)).save(any());
    }

    // Delete User Tests.
    @Test
    public void deleteUser_deletesUserCorrectly() {
        List<UserRoleEntity> userRoleEntities = List.of(createUserRoleEntity());
        UserEntity userEntity = createUserEntity();
        UserCustomFieldEntity userCustomFieldEntity = createDropDownUserCustomFieldEntity();
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
        userEntity.get().setCustomFields(createUserCustomFieldEntityList());
        UserDTO user = createUserDto();
        user.setAssignedRoles(List.of(createRoleDto()));
        user.setCustomFields(userEntity.get().getCustomFields()
                .stream().map(MapperUtils::mapUserCustomFieldEntityToDto)
                .collect(Collectors.toList()));

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

    @Test
    public void updateCustomField_succeeds_if_valid() {
        UserEntity user = createUserEntity();
        CustomFieldEntity customFieldEntity = createDropDownUserCustomFieldEntity().getCustomField();
        CreateOrUpdateUserCustomFieldDTO dto = new CreateOrUpdateUserCustomFieldDTO();
        dto.setCustomFieldId(customFieldEntity.getId());
        dto.setValue("VALUE_1");
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customFieldEntity));
        when(userCustomFieldRepository.findFirstByUserAndCustomField(any(), any())).thenReturn(Optional.empty());

        ResponseEntity<Void> res = userService.updateCustomField(user.getId(), dto);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userCustomFieldRepository).save(userCustomFieldCaptor.capture());
        UserCustomFieldEntity savedUserCustomFieldEntity = userCustomFieldCaptor.getValue();
        assertEquals(savedUserCustomFieldEntity.getCustomFieldValueString(), dto.getValue().toString());
        assertEquals(dto.getCustomFieldId(), savedUserCustomFieldEntity.getCustomField().getId());
    }

    @Test
    public void updateCustomField_fails_if_user_does_not_exist() {
        when(userRepository.findById(any())).thenReturn(Optional.empty());
        CreateOrUpdateUserCustomFieldDTO customField = new CreateOrUpdateUserCustomFieldDTO();
        customField.setCustomFieldId(UUID.randomUUID());
        customField.setValue("VALUE_1");
        UserEntity user = createUserEntity();
        when(userRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
            userService.updateCustomField(UUID.randomUUID(), customField));
        assertEquals(exception.getMessage(), "User not found!");
    }

    @Test
    public void updateCustomField_fails_if_field_does_not_exist() {
        UserEntity user = createUserEntity();
        when(userRepository.findById(any())).thenReturn(Optional.of(user));
        when(customFieldRepository.findById(any())).thenReturn(Optional.empty());
        CreateOrUpdateUserCustomFieldDTO customField = new CreateOrUpdateUserCustomFieldDTO();
        customField.setCustomFieldId(UUID.randomUUID());
        customField.setValue("VALUE_1");

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                userService.updateCustomField(UUID.randomUUID(), customField));
        assertEquals(exception.getMessage(), "Custom field not found!");
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

    private List<UserCustomFieldEntity> createUserCustomFieldEntityList() {
        return List.of(
                createDropDownUserCustomFieldEntity(),
                createStringTextFieldUserCustomFieldEntity(),
                createJsonTextFieldUserCustomFieldEntity(),
                createDateTimeTextFieldUserCustomFieldEntity(),
                createIntTextFieldUserCustomFieldEntity()
        );
    }

    private UserEntity createUserEntity() {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("Skipper@theIsland.com");
        userEntity.setDisplayName("John Locke");
        userEntity.setExternalId("TestExternalId1234");
        userEntity.setId(UUID.fromString("ca8cfd1b-8643-4185-ba7f-8c8fbc9a7da6"));
        return userEntity;
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
        user.setId(userEntity.getId());
        return user;
    }

    private UserRoleDTO createUserRoleDto() {
        UserRoleDTO userRoleDto = new UserRoleDTO();
        userRoleDto.setRoleId(createRoleDto().getId());
        userRoleDto.setUserId(createUserEntity().getId());
        return userRoleDto;
    }

    private UserCustomFieldEntity createDropDownUserCustomFieldEntity() {
        CustomFieldEntity customFieldEntity = new CustomFieldEntity();
        customFieldEntity.setId(UUID.randomUUID());
        customFieldEntity.setName("CUSTOM_FIELD_1");
        customFieldEntity.setType(getDropDownTypeEntity());
        customFieldEntity.setDataType(getStringDataTypeEntity());
        customFieldEntity.setDisplayText("Custom Field 1");

        UserCustomFieldEntity userCustomFieldEntity = new UserCustomFieldEntity();
        userCustomFieldEntity.setId(UUID.randomUUID());
        userCustomFieldEntity.setCustomField(customFieldEntity);
        userCustomFieldEntity.setCustomFieldValueString("TEST1");

        return userCustomFieldEntity;
    }

    private UserCustomFieldEntity createStringTextFieldUserCustomFieldEntity() {
        CustomFieldEntity customFieldEntity = new CustomFieldEntity();
        customFieldEntity.setId(UUID.randomUUID());
        customFieldEntity.setName("CUSTOM_FIELD_2");
        customFieldEntity.setType(getTextFieldTypeEntity());
        customFieldEntity.setDataType(getStringDataTypeEntity());
        customFieldEntity.setDisplayText("Custom Field 2");

        UserCustomFieldEntity userCustomFieldEntity = new UserCustomFieldEntity();
        userCustomFieldEntity.setId(UUID.randomUUID());
        userCustomFieldEntity.setCustomField(customFieldEntity);
        userCustomFieldEntity.setCustomFieldValueString("TEST1");

        return userCustomFieldEntity;
    }

    private UserCustomFieldEntity createJsonTextFieldUserCustomFieldEntity() {
        CustomFieldEntity customFieldEntity = new CustomFieldEntity();
        customFieldEntity.setId(UUID.randomUUID());
        customFieldEntity.setName("CUSTOM_FIELD_3");
        customFieldEntity.setType(getTextFieldTypeEntity());
        customFieldEntity.setDataType(getJsonDataTypeEntity());
        customFieldEntity.setDisplayText("Custom Field 3");

        UserCustomFieldEntity userCustomFieldEntity = new UserCustomFieldEntity();
        userCustomFieldEntity.setId(UUID.randomUUID());
        userCustomFieldEntity.setCustomField(customFieldEntity);

        UserCustomFieldDTO userCustomField = new UserCustomFieldDTO();
        userCustomField.setName("test1");
        userCustomField.setValue("test1");
        userCustomField.setDisplayText("test1");
        try {
            userCustomFieldEntity.setCustomFieldValueJson(new ObjectMapper().writeValueAsString(userCustomField));
        } catch (JsonProcessingException e) {
            userCustomFieldEntity.setCustomFieldValueJson(null);
        }
        return userCustomFieldEntity;
    }

    private UserCustomFieldEntity createIntTextFieldUserCustomFieldEntity() {
        CustomFieldEntity customFieldEntity = new CustomFieldEntity();
        customFieldEntity.setId(UUID.randomUUID());
        customFieldEntity.setName("CUSTOM_FIELD_4");
        customFieldEntity.setType(getTextFieldTypeEntity());
        customFieldEntity.setDataType(getIntDataTypeEntity());
        customFieldEntity.setDisplayText("Custom Field 4");

        UserCustomFieldEntity userCustomFieldEntity = new UserCustomFieldEntity();
        userCustomFieldEntity.setId(UUID.randomUUID());
        userCustomFieldEntity.setCustomField(customFieldEntity);
        userCustomFieldEntity.setCustomFieldValueInt(123);

        return userCustomFieldEntity;
    }

    private UserCustomFieldEntity createDateTimeTextFieldUserCustomFieldEntity() {
        CustomFieldEntity customFieldEntity = new CustomFieldEntity();
        customFieldEntity.setId(UUID.randomUUID());
        customFieldEntity.setName("CUSTOM_FIELD_5");
        customFieldEntity.setType(getTextFieldTypeEntity());
        customFieldEntity.setDataType(getDateTimeDataTypeEntity());
        customFieldEntity.setDisplayText("Custom Field 5");

        UserCustomFieldEntity userCustomFieldEntity = new UserCustomFieldEntity();
        userCustomFieldEntity.setId(UUID.randomUUID());
        userCustomFieldEntity.setCustomField(customFieldEntity);
        userCustomFieldEntity.setCustomFieldValueDateTime(OffsetDateTime.now(Clock.systemDefaultZone()));

        return userCustomFieldEntity;
    }

    private List<CreateOrUpdateUserCustomFieldDTO> getCreateUserCustomFieldList(
            List<UserCustomFieldEntity> userCustomFieldEntityList) {
        return userCustomFieldEntityList.stream().map(c -> {
            CreateOrUpdateUserCustomFieldDTO dto = new CreateOrUpdateUserCustomFieldDTO();
            dto.setCustomFieldId(c.getCustomField().getId());
            switch (CustomFieldDataType.fromText(c.getCustomField().getDataType().getType())) {
                case JSON:
                    dto.setValue(new ObjectMapper().convertValue(c.getCustomFieldValueJson(), Object.class));
                    break;
                case DATETIME:
                    dto.setValue(c.getCustomFieldValueDateTime());
                    break;
                case INT:
                    dto.setValue(c.getCustomFieldValueInt());
                    break;
                case STRING:
                default:
                    dto.setValue(c.getCustomFieldValueString());
                    break;
            }
            return dto;
        }).collect(Collectors.toList());
    }

    private CustomFieldTypeEntity getDropDownTypeEntity() {
        CustomFieldTypeEntity customFieldTypeEntity = new CustomFieldTypeEntity();
        customFieldTypeEntity.setId(UUID.fromString("a80a48b5-2995-4c54-9bd5-ebc258fab4ba"));
        customFieldTypeEntity.setType("drop_down_list");
        return customFieldTypeEntity;
    }

    private CustomFieldTypeEntity getTextFieldTypeEntity() {
        CustomFieldTypeEntity customFieldTypeEntity = new CustomFieldTypeEntity();
        customFieldTypeEntity.setId(UUID.fromString("5fc38fee-e8f5-11ec-8fea-0242ac120002"));
        customFieldTypeEntity.setType("text_field");
        return customFieldTypeEntity;
    }

    private CustomFieldDataTypeEntity getStringDataTypeEntity() {
        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("3e724ddf-4d09-452b-ae98-a8e3a76af19c"));
        customFieldDataTypeEntity.setType("string");
        return customFieldDataTypeEntity;
    }

    private CustomFieldDataTypeEntity getIntDataTypeEntity() {
        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("7c6e4de3-5461-4a38-bdf8-2f853c50e3a3"));
        customFieldDataTypeEntity.setType("int");
        return customFieldDataTypeEntity;
    }

    private CustomFieldDataTypeEntity getJsonDataTypeEntity() {
        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("69af3a92-b6d6-4b6a-90e5-51304cba887c"));
        customFieldDataTypeEntity.setType("json");
        return customFieldDataTypeEntity;
    }

    private CustomFieldDataTypeEntity getDateTimeDataTypeEntity() {
        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("5d01d9e3-f8a8-42e3-877c-b743bff79e7f"));
        customFieldDataTypeEntity.setType("datetime");
        return customFieldDataTypeEntity;
    }

    /*** Helper function for use when validating that the update user function works as expected.
     * Asserts should cause the test to fail, removing the need for any return value as success is expected.
     *
     * @param originalEntity a UserEntity object that contains the original values to be compared against
     * @param updateRequest a UserUpdateRequest object that contains the values to confirm as changed
     * @param newEntity a UserEntity object that will be tested against originalEntity and updateRequest for changes
     * @param newDTO a UserDTO object that will be tested against originalEntity and updateRequest for changes
     */
    private void validateUserUpdate(
        UserEntity originalEntity,
        UserUpdateRequest updateRequest,
        UserEntity newEntity,
        UserDTO newDTO) {

        // Check display name
        if (updateRequest.getDisplayName() != null) {
            // In update request, should be changed
            assertEquals(updateRequest.getDisplayName(), newEntity.getDisplayName());
            assertEquals(updateRequest.getDisplayName(), newDTO.getDisplayName());
        } else {
            // Not in update request, should match original
            assertEquals(originalEntity.getDisplayName(), newEntity.getDisplayName());
            assertEquals(originalEntity.getDisplayName(), newDTO.getDisplayName());
        }

        // Check email
        if (updateRequest.getEmail() != null) {
            // In update request, should be changed
            assertEquals(updateRequest.getEmail(), newEntity.getEmail());
            assertEquals(updateRequest.getEmail(), newDTO.getEmail());
        } else {
            // Not in update request, should match original
            assertEquals(originalEntity.getEmail(), newEntity.getEmail());
            assertEquals(originalEntity.getEmail(), newDTO.getEmail());
        }

        // Check external id
        if (updateRequest.getExternalId() != null) {
            // In update request, should be changed
            assertEquals(updateRequest.getExternalId(), newEntity.getExternalId());
            assertEquals(updateRequest.getExternalId(), newDTO.getExternalId());
        } else {
            // Not in update request, should match original
            assertEquals(originalEntity.getExternalId(), newEntity.getExternalId());
            assertEquals(originalEntity.getExternalId(), newDTO.getExternalId());
        }

        // Also validate that other parameters exist and are unchanged
        assertEquals(newEntity.getId(), originalEntity.getId());
        assertEquals(newDTO.getId(), originalEntity.getId());
        assertNull(newDTO.getAssignedRoles());
        assertNull(newEntity.getUserRoleEntities());
        assertNull(newDTO.getPreferences());
    }

}
