package io.nuvalence.user.management.api.service.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.CustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserCustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import io.nuvalence.user.management.api.service.enums.CustomFieldDataType;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateUserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.generated.models.UserRoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserUpdateRequest;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.mapper.UserEntityMapper;
import io.nuvalence.user.management.api.service.repository.CustomFieldRepository;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserCustomFieldRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import io.nuvalence.user.management.api.service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for User.
 */

@Component
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final CustomFieldRepository customFieldRepository;
    private final UserCustomFieldRepository userCustomFieldRepository;
    private final RoleRepository roleRepository;
    private final CerbosClient client;

    /**
     * Creates a User Entity from a user model.
     *
     * @param user represents a user model
     * @return a response code
     */
    @SuppressWarnings("checkstyle:CyclomaticComplexity")
    public ResponseEntity<Void> createUser(UserCreationRequest user) {
        if (StringUtils.isBlank(user.getExternalId())) {
            throw new BusinessLogicException("Missing identifier for user: " + user.getEmail());
        }

        Optional<UserEntity> checkEmail = userRepository.findUserEntityByEmail(user.getEmail());
        if (checkEmail.isPresent()) {
            throw new BusinessLogicException("This Email is already assigned to a user.");
        }

        Optional<UserEntity> checkExternalId = userRepository.findUserEntityByExternalId(user.getExternalId());
        if (checkExternalId.isPresent()) {
            throw new BusinessLogicException("This ExternalId is already assigned to a user.");
        }

        UserEntity userEntity = UserEntityMapper.INSTANCE.convertUserCreationRequestToUserEntity(user);
        // Setting the created at time to America NY to prevent any EST / EDT conversion errors.
        LocalDateTime currentDateAndTime = LocalDateTime.from(Instant.now().atZone(ZoneId.of("America/New_York")));
        userEntity.setCreatedAt(currentDateAndTime);
        UserEntity savedUser = userRepository.save(userEntity);

        // If roles are provided we want to initialize them here.
        if (user.getInitialRoles() != null && !user.getInitialRoles().isEmpty()) {
            List<UserRoleEntity> userRoleEntities = roleRepository.findAllById(user.getInitialRoles()
                    .stream().map(RoleDTO::getId).collect(Collectors.toList()))
                    .stream().map(r -> {
                        UserRoleEntity userRoleEntity = new UserRoleEntity();
                        userRoleEntity.setUser(savedUser);
                        userRoleEntity.setRole(r);
                        return userRoleEntity;
                    }).collect(Collectors.toList());
            Optional<RoleDTO> notFoundRole = user.getInitialRoles()
                    .stream().filter(r -> userRoleEntities.stream().noneMatch(re ->
                            re.getRole().getId().compareTo(r.getId()) == 0))
                    .findFirst();
            if (notFoundRole.isPresent()) {
                throw new BusinessLogicException(
                        String.format("No role found for %s.", notFoundRole.get().getRoleName())
                );
            }
            userRoleRepository.saveAll(userRoleEntities);
        }

        if (user.getCustomFields() != null && !user.getCustomFields().isEmpty()) {
            Map<UUID, CreateOrUpdateUserCustomFieldDTO> userCustomFields = user.getCustomFields()
                    .stream().collect(Collectors.toMap(CreateOrUpdateUserCustomFieldDTO::getCustomFieldId, c -> c));
            List<UserCustomFieldEntity> userCustomFieldEntities = customFieldRepository
                    .findAllById(user.getCustomFields()
                    .stream().map(CreateOrUpdateUserCustomFieldDTO::getCustomFieldId).collect(Collectors.toList()))
                    .stream().map(c -> {
                        UserCustomFieldEntity userCustomField = UserCustomFieldEntity.builder()
                                .user(savedUser)
                                .customField(c)
                                .build();
                        try {
                            setUserCustomFieldValueFromCustomFieldDto(c, userCustomField,
                                    userCustomFields.get(c.getId()));
                        } catch (Exception e) {
                            log.error("Exception occurred at UserService.createUser: {}", e.getMessage());
                        }
                        return userCustomField;
                    }).collect(Collectors.toList());
            Optional<CreateOrUpdateUserCustomFieldDTO> notFoundCustomField = user.getCustomFields()
                    .stream().filter(c -> userCustomFieldEntities.stream()
                            .noneMatch(cf -> cf.getCustomField().getId().compareTo(c.getCustomFieldId()) == 0)
                    )
                    .findFirst();
            if (notFoundCustomField.isPresent()) {
                throw new BusinessLogicException(
                        String.format("No custom field found with id: %s.",
                                notFoundCustomField.get().getCustomFieldId())
                );
            }
            userCustomFieldRepository.saveAll(userCustomFieldEntities);
        }

        return ResponseEntity.status(200).build();
    }

    /**
     * Updates a User Entity from a (partial) user model.
     *
     * @param userId is a user's userId
     * @param updateRequest is a user update request with changes to be made
     * @return the updated UserDTO object in a ResponseEntity
     */
    public ResponseEntity<UserDTO> updateUserById(UUID userId, UserUpdateRequest updateRequest) {
        // Validate that userId has been provided and corresponds to an actual user
        if (userId == null) {
            throw new ResourceNotFoundException("Missing user Id.");
        }
        Optional<UserEntity> foundUserEntity = userRepository.findById(userId);
        if (foundUserEntity.isEmpty()) {
            throw new ResourceNotFoundException("User not found.");
        }
        UserEntity userEntity = foundUserEntity.get();

        // Update displayName if provided
        if (updateRequest.getDisplayName() != null) {
            userEntity.setDisplayName(updateRequest.getDisplayName());
        }

        // Update external ID if provided
        if (updateRequest.getExternalId() != null
            && !updateRequest.getExternalId().equals(userEntity.getExternalId())) {
            // Validate external ID does not match with any existing users
            Optional<UserEntity> checkExternalId =
                userRepository.findUserEntityByExternalId(updateRequest.getExternalId());
            if (checkExternalId.isPresent()) {
                throw new BusinessLogicException("This ExternalId is already assigned to a user.");
            }
            userEntity.setExternalId(updateRequest.getExternalId());
        }

        // Update email if provided
        if (updateRequest.getEmail() != null
            && !updateRequest.getEmail().equals(userEntity.getEmail())) {
            // Validate email does not match with any existing users
            Optional<UserEntity> checkEmail = userRepository.findUserEntityByEmail(updateRequest.getEmail());
            if (checkEmail.isPresent()) {
                throw new BusinessLogicException("This Email is already assigned to a user.");
            }
            userEntity.setEmail(updateRequest.getEmail());
        }

        UserEntity savedUser = userRepository.save(userEntity);
        UserDTO userDTO = UserEntityMapper.INSTANCE.convertUserEntityToUserModel(savedUser);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userDTO);
    }

    /**
     * Deletes a user entity from the DB.
     * @param userId is a user's id
     * @return a response code
     */
    public ResponseEntity<Void> deleteUser(UUID userId) {
        List<UserRoleEntity> userRoleEntities = userRoleRepository.findAllByUserId(userId);
        List<UserCustomFieldEntity> userCustomFieldEntities = userCustomFieldRepository.findAllByUserId(userId);
        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if (userEntity.isEmpty()) {
            throw new ResourceNotFoundException("User not found.");
        }
        userCustomFieldRepository.deleteAll(userCustomFieldEntities);
        userRoleRepository.deleteAll(userRoleEntities);
        userRepository.delete(userEntity.get());

        return ResponseEntity.status(200).build();
    }

    /**
     * Assigns a tole to a particular user.
     *
     * @param userRole is a user role dto
     * @return a response code for the call.
     */
    public ResponseEntity<Void> assignRoleToUser(UserRoleDTO userRole) {

        Optional<UserEntity> userEntity = userRepository.findById(userRole.getUserId());
        Optional<RoleEntity> roleEntity = roleRepository.findById(userRole.getRoleId());

        if (userEntity.isEmpty() || roleEntity.isEmpty()) {
            throw new ResourceNotFoundException("User or Role not found.");
        }

        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUser(userEntity.get());
        userRoleEntity.setRole(roleEntity.get());
        userRoleRepository.save(userRoleEntity);

        return ResponseEntity.status(200).build();
    }

    /**
     * Deletes role for a particular user.
     *
     * @param userRole is a DTO for a user role linkage
     * @return a response code for the call.
     */
    public ResponseEntity<Void> removeRoleFormUser(UserRoleDTO userRole) {
        Optional<UserEntity> userEntity = userRepository.findById(userRole.getUserId());
        Optional<RoleEntity> roleEntity = roleRepository.findById(userRole.getRoleId());

        if (userEntity.isEmpty() || roleEntity.isEmpty()) {
            throw new ResourceNotFoundException("User or Role not found.");
        }

        UserRoleEntity userRoleEntity = userRoleRepository.findByUserAndRole(userEntity.get(), roleEntity.get());
        if (userRoleEntity == null) {
            throw new BusinessLogicException(String.format("The role requested does not exist on %s.",
                    userEntity.get().getDisplayName()));
        }
        userRoleRepository.delete(userRoleEntity);

        return ResponseEntity.status(200).build();
    }

    /**
     * Returns a list of roles based on the user's id.
     *
     * @param userId is a user's id.
     * @param resourceName The name of the resource.
     * @return a status code.
     */
    public ResponseEntity<List<RoleDTO>> fetchRolesByUserId(UUID userId, String resourceName) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if (userEntity.isEmpty()) {
            throw new ResourceNotFoundException("User not found!");
        }
        List<RoleEntity> roleEntities = userEntity.get().getUserRoleEntities().stream()
                .map(UserRoleEntity::getRole).collect(Collectors.toList());

        Map<String, String[]> rolePermissionMappings = client.getRolePermissionMappings(resourceName);
        List<RoleDTO> roles = MapperUtils.mapRoleEntitiesToRoleList(roleEntities, rolePermissionMappings);
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(roles);
    }

    /**
     * Find all users in a List.
     *
     * @param resourceName The name of the resource.
     * @return a list of roles based on the user's id.
     */
    public ResponseEntity<List<UserDTO>> getUserList(String resourceName) {

        List<UserEntity> allUsers = userRepository.findAll();

        if (allUsers.isEmpty()) {
            throw new ResourceNotFoundException("No users found.");
        }

        Map<String, String[]> rolePermissionMappings = client.getRolePermissionMappings(resourceName);

        List<UserDTO> users = allUsers.stream().map(u -> {
            UserDTO user = UserEntityMapper.INSTANCE.convertUserEntityToUserModel(u);
            user.setAssignedRoles(MapperUtils.mapUserEntityToRoleList(u, rolePermissionMappings));
            return user;
        }).collect(Collectors.toList());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(users);
    }

    /**
     * Returns a user by the user's id.
     *
     * @param userId is a user's id.
     * @param resourceName The name of the resource.
     * @return a status code and a user.
     */
    public ResponseEntity<UserDTO> fetchUserById(UUID userId, String resourceName) {
        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if (userEntity.isEmpty()) {
            throw new ResourceNotFoundException("User not found!");
        }

        Map<String, String[]> rolePermissionMappings = client.getRolePermissionMappings(resourceName);

        UserDTO user = UserEntityMapper.INSTANCE.convertUserEntityToUserModel(userEntity.get());
        user.setAssignedRoles(MapperUtils.mapUserEntityToRoleList(userEntity.get(), rolePermissionMappings));
        user.setCustomFields(MapperUtils.mapUserEntityToCustomFieldDtoList(userEntity.get()));

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(user);
    }

    /**
     * Finds a user by their email.
     *
     * @param email        user's email.
     * @param resourceName The name of the resource.
     * @return a user dto.
     */
    public ResponseEntity<UserDTO> fetchUserByEmail(String email, String resourceName) {
        Optional<UserEntity> user = userRepository.findUserEntityByEmail(email);

        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found!");
        }

        Map<String, String[]> rolePermissionMappings = client.getRolePermissionMappings(resourceName);

        UserDTO userDto = UserEntityMapper.INSTANCE.convertUserEntityToUserModel(user.get());
        userDto.setAssignedRoles(MapperUtils.mapUserEntityToRoleList(user.get(), rolePermissionMappings));
        userDto.setCustomFields(MapperUtils.mapUserEntityToCustomFieldDtoList(user.get()));
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userDto);
    }

    /**
     * Updates (or creates if it doesn't already exist) a custom field with the provided value.
     *
     * @param userId the id of the user.
     * @param userCustomField is a DTO of the user custom field to be updated/created.
     * @return a status code
     * @throws Exception if the provided json could not be serialized
     */
    public ResponseEntity<Void> updateCustomField(UUID userId, CreateOrUpdateUserCustomFieldDTO userCustomField) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("User not found!");
        }

        Optional<CustomFieldEntity> customField = customFieldRepository.findById(userCustomField.getCustomFieldId());
        if (customField.isEmpty()) {
            throw new ResourceNotFoundException("Custom field not found!");
        }

        Optional<UserCustomFieldEntity> userCustomFieldEntity = userCustomFieldRepository
                .findFirstByUserAndCustomField(userId, userCustomField.getCustomFieldId());

        if (userCustomFieldEntity.isEmpty()) {
            userCustomFieldEntity = Optional.of(UserCustomFieldEntity.builder()
                            .user(user.get())
                            .customField(customField.get())
                            .build());
        }

        try {
            setUserCustomFieldValueFromCustomFieldDto(customField.get(), userCustomFieldEntity.get(), userCustomField);
            userCustomFieldRepository.save(userCustomFieldEntity.get());
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            log.error("Exception occurred in UserService.updateCustomField: {}", ex.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    private void setUserCustomFieldValueFromCustomFieldDto(CustomFieldEntity customField,
                                                           UserCustomFieldEntity userCustomFieldEntity,
                                                           CreateOrUpdateUserCustomFieldDTO userCustomField)
            throws Exception {
        switch (CustomFieldDataType.fromText(customField.getDataType().getType())) {
            case INT:
                userCustomFieldEntity.setCustomFieldValueInt((Integer)userCustomField.getValue());
                break;
            case JSON:
                if (userCustomField.getValue() != null) {
                    userCustomFieldEntity.setCustomFieldValueJson(
                            new ObjectMapper().writeValueAsString(userCustomField.getValue())
                    );
                } else {
                    userCustomFieldEntity.setCustomFieldValueJson(null);
                }
                break;
            case DATETIME:
                if (userCustomField.getValue() != null) {
                    userCustomFieldEntity.setCustomFieldValueDateTime(
                            OffsetDateTime.parse(
                                    userCustomField.getValue().toString(), DateTimeFormatter.ISO_DATE_TIME)
                    );
                } else {
                    userCustomFieldEntity.setCustomFieldValueDateTime(null);
                }
                break;
            default:
            case STRING:
                userCustomFieldEntity.setCustomFieldValueString((String)userCustomField.getValue());
                break;
        }
    }
}
