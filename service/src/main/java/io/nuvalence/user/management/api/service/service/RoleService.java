package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.mapper.RoleEntityMapper;
import io.nuvalence.user.management.api.service.mapper.UserEntityMapper;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Role.
 */

@Component
@RequiredArgsConstructor
@Slf4j
public class RoleService {

    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;
    private final CerbosClient client;

    /**
     * Adds a role to the database.
     *
     * @param role indicates a role dto.
     * @return a status code.
     */
    public ResponseEntity<Void> addRole(RoleDTO role) {
        RoleEntity nameCheck = roleRepository.findByRoleName(role.getRoleName().toUpperCase());
        if (nameCheck != null) {
            throw new BusinessLogicException("This role already exists.");
        }

        RoleEntity newRole = RoleEntityMapper.INSTANCE.roleDtoToRoleEntity(role);
        // Ensures uppercase on submission
        newRole.setRoleName(role.getRoleName().toUpperCase());
        roleRepository.save(newRole);

        return ResponseEntity.ok().build();
    }

    /**
     * Updates a role.
     * @param roleId The id of the role
     * @param role The role object
     * @param resourceName The name of the resource
     * @return a status code
     */
    public ResponseEntity<Void> updateRole(UUID roleId, RoleDTO role, String resourceName) {
        Optional<RoleEntity> idCheck = roleRepository.findById(roleId);
        if (idCheck.isEmpty()) {
            throw new BusinessLogicException("This role does not exist.");
        }

        // TODO: validate that the permissions passed in are actual permissions (this is a placeholder)
        if (role.getPermissions() != null
                && role.getPermissions().size() > 0
                && role.getPermissions().contains("permissionToFail")) {
            throw new BusinessLogicException("The permission(s) are invalid.");
        }

        client.updateRolePermissionMappings(resourceName, role.getRoleName(),
                role.getPermissions().toArray(String[]::new));
        return ResponseEntity.ok().build();
    }

    /**
     * Fetches a list of all roles that exist and their permissions relative to a resource.
     * @param resourceName The name of the resource.
     * @return a list of all the roles.
     */
    public ResponseEntity<List<RoleDTO>> getAllRolesByResource(String resourceName) {
        List<RoleEntity> roleEntities = roleRepository.findAll();

        Map<String, String[]> rolePermissionMappings = client.getRolePermissionMappings(resourceName);
        List<RoleDTO> roles = MapperUtils.mapRoleEntitiesToRoleList(roleEntities, rolePermissionMappings);

        // TODO: find permissions by roles
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(roles);
    }

    /**
     * Fetches a list of all roles that exist.
     *
     * @return a list of all the roles.
     */
    public ResponseEntity<List<RoleDTO>> getAllRoles() {
        List<RoleEntity> roleEntities = roleRepository.findAll();

        List<RoleDTO> roles = MapperUtils.mapRoleEntitiesToRoleList(roleEntities);

        // TODO: find permissions by roles
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(roles);
    }

    /**
     * Deletes a role by its id.
     *
     * @param roleId is an id of a role.
     * @param resourceName The name of the resource.
     * @return a status code.
     */
    public ResponseEntity<Void> deleteRoleById(UUID roleId, String resourceName) {
        Optional<RoleEntity> roleEntity = roleRepository.findById(roleId);
        if (roleEntity.isEmpty()) {
            throw new ResourceNotFoundException("There is no role that exists with this id.");
        }
        roleRepository.delete(roleEntity.get());

        client.removeRole(resourceName, roleEntity.get().getRoleName());
        return ResponseEntity.ok().build();
    }

    /**
     * Returns a list of users that contain the checked role.
     *
     * @param roleId describes a role id.
     * @param resourceName The name of the resource.
     * @return a list of userDTOs that have that role.
     */
    public ResponseEntity<List<UserDTO>> getUsersByRoleId(UUID roleId, String resourceName) {
        Optional<RoleEntity> roleCheck = roleRepository.findById(roleId);

        if (roleCheck.isEmpty()) {
            throw new ResourceNotFoundException("There is no role with this id");
        }

        Map<String, String[]> rolePermissionMappings = client.getRolePermissionMappings(resourceName);

        List<UserDTO> users = userRoleRepository.findAllByRoleId(roleId).stream().map(userRole -> {
            UserDTO user = UserEntityMapper.INSTANCE.convertUserEntityToUserModel(userRole.getUser());
            user.setAssignedRoles(MapperUtils.mapUserEntityToRoleList(userRole.getUser(), rolePermissionMappings));
            return user;
        }).collect(Collectors.toList());

        if (users.isEmpty()) {
            throw new BusinessLogicException("There are no users with this role");
        }
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(users);
    }
}
