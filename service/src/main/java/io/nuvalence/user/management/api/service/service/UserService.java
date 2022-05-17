package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.generated.models.UserRoleDTO;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.mapper.UserEntityMapper;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import io.nuvalence.user.management.api.service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

/**
 * Service for User.
 */

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;
    private final CerbosClient client;

    /**
     * Creates a User Entity from a user model.
     *
     * @param user represents a user model
     * @return a response code
     */
    public ResponseEntity<Void> createUser(UserCreationRequest user) {
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
        if (!user.getInitialRoles().isEmpty()) {
            List<RoleEntity> roleEntities = user.getInitialRoles().stream().map(role -> {
                RoleEntity roleEntity = roleRepository.getById(role.getId());
                if (roleEntity == null) {
                    throw new BusinessLogicException(String.format("No role found for %s.", role.getRoleName()));
                }
                return roleEntity;
            }).collect(Collectors.toList());


            roleEntities.forEach(role -> {
                UserRoleEntity userRoleEntity = new UserRoleEntity();
                userRoleEntity.setUser(savedUser);
                userRoleEntity.setRole(role);
                userRoleRepository.save(userRoleEntity);
            });
        }

        return ResponseEntity.status(200).build();
    }

    /**
     * Deletes a user entity from the DB.
     *
     * @param userId is a user's id
     * @return a response code
     */
    public ResponseEntity<Void> deleteUser(UUID userId) {
        List<UserRoleEntity> userRoleEntities = userRoleRepository.findAllByUserId(userId);
        Optional<UserEntity> userEntity = userRepository.findById(userId);

        if (userEntity.isEmpty()) {
            throw new ResourceNotFoundException("User not found.");
        }
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
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(userDto);
    }
}
