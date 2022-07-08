package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationPermissionEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleApplicationDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleUpdateRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.mapper.RoleEntityMapper;
import io.nuvalence.user.management.api.service.mapper.UserEntityMapper;
import io.nuvalence.user.management.api.service.repository.ApplicationPermissionRepository;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
import io.nuvalence.user.management.api.service.repository.RoleRepository;
import io.nuvalence.user.management.api.service.repository.UserRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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
    private final ApplicationRepository applicationRepository;
    private final PermissionRepository permissionRepository;
    private final ApplicationPermissionRepository applicationPermissionRepository;
    private final UserRoleRepository userRoleRepository;
    private final CerbosClient client;

    /**
     * Adds a role to the database.
     *
     * @param roleCreationRequest the role creation request object
     * @return a status code.
     */
    public ResponseEntity<Void> addRole(RoleCreationRequest roleCreationRequest) {
        RoleEntity nameCheck = roleRepository.findByRoleName(roleCreationRequest.getRoleName().toUpperCase());
        if (nameCheck != null) {
            throw new BusinessLogicException("This role already exists.");
        }

        RoleEntity newRole = RoleEntityMapper.INSTANCE.roleCreationRequestToRoleEntity(roleCreationRequest);

        // Ensures uppercase on submission
        newRole.setRoleName(roleCreationRequest.getRoleName().toUpperCase());

        // Validate the applications and permissions
        Map<UUID, ApplicationEntity> applicationEntityMap =
            validateApplicationsAndPermissions(roleCreationRequest.getApplications());

        // Save each application's permissions
        saveApplicationPermissions(roleCreationRequest.getApplications(), applicationEntityMap,
            roleCreationRequest.getRoleName());

        roleRepository.save(newRole);
        return ResponseEntity.ok().build();
    }

    /**
     * Updates a role.
     * @param roleId The id of the role
     * @param roleUpdateRequest The role update request object
     * @return a status code
     */
    public ResponseEntity<Void> updateRole(UUID roleId, RoleUpdateRequest roleUpdateRequest) {
        Optional<RoleEntity> idCheck = roleRepository.findById(roleId);
        if (idCheck.isEmpty()) {
            throw new BusinessLogicException("This role does not exist.");
        }
        RoleEntity role = idCheck.get();

        // Update the role name if applicable
        if (roleUpdateRequest.getRoleName() != null) {
            // Ensures uppercase on submission
            role.setRoleName(roleUpdateRequest.getRoleName().toUpperCase());
        }

        // Update the role display name if applicable
        if (roleUpdateRequest.getDisplayName() != null) {
            role.setDisplayName(roleUpdateRequest.getDisplayName());
        }

        // Update the role application permissions if applicable

        // Validate the applications and permissions
        Map<UUID, ApplicationEntity> applicationEntityMap =
            validateApplicationsAndPermissions(roleUpdateRequest.getApplications());

        // Save each application's permissions
        saveApplicationPermissions(roleUpdateRequest.getApplications(), applicationEntityMap, role.getRoleName());

        roleRepository.save(role);
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

    private Map<UUID, ApplicationEntity> validateApplicationsAndPermissions(
        List<RoleApplicationDTO> roleApplicationDTOs) {
        // Validate the applications and permissions
        if (roleApplicationDTOs != null) {
            // Check the validity of the applications

            // Create a list of all application IDs
            ArrayList<UUID> appIds = new ArrayList<>();
            roleApplicationDTOs.forEach((app) -> {
                // Make sure the application has an ID, then add it to our application ID list
                if (app.getApplicationId() == null) {
                    throw new BusinessLogicException("Application id not provided.");
                } else {
                    appIds.add(app.getApplicationId());
                }
            });

            // Check the list against the application repository
            List<ApplicationEntity> applications = applicationRepository.findAllById(appIds);
            List<UUID> foundApplications = applications.stream().map(ApplicationEntity::getId)
                .collect(Collectors.toList());
            Optional<UUID> notFoundApplication = appIds.stream()
                .filter(a -> !foundApplications.contains(a))
                .findFirst();
            if (notFoundApplication.isPresent()) {
                throw new BusinessLogicException("The provided application id '"
                    + notFoundApplication.get() + "' is invalid.");
            }
            Map<UUID, ApplicationEntity> applicationEntityMap = applications.stream().collect(Collectors.toMap(
                ApplicationEntity::getId,
                e -> e
            ));

            // Validate the permissions
            List<ApplicationPermissionEntity> appPerms = applicationPermissionRepository.findAll();
            for (RoleApplicationDTO raDTO : roleApplicationDTOs) {
                if (raDTO.getPermissions() != null) {
                    // Make sure each permission exists
                    for (String permission : raDTO.getPermissions()) {
                        Optional<PermissionEntity> perm = permissionRepository.findByPermissionName(permission);
                        if (perm.isEmpty()) {
                            throw new BusinessLogicException("The provided permission '" + permission
                                + "' is invalid.");
                        }

                        boolean appPermExists = appPerms.stream()
                            .anyMatch(appPerm -> perm.get().getId().equals(appPerm.getPermission().getId())
                                && applicationEntityMap.get(raDTO.getApplicationId()).getId()
                                .equals(appPerm.getApplication().getId()));
                        if (!appPermExists) {
                            throw new BusinessLogicException("The provided permission '" + permission
                                + "' is invalid for the provided application '"
                                + applicationEntityMap.get(raDTO.getApplicationId()).getDisplayName() + "'.");
                        }
                    }
                }
            }
            return applicationEntityMap;
        }
        return null;
    }

    private void saveApplicationPermissions(List<RoleApplicationDTO> roleApplicationDTOs,
                                            Map<UUID, ApplicationEntity> applicationEntityMap,
                                            String roleName) {
        for (RoleApplicationDTO raDTO : roleApplicationDTOs) {
            ApplicationEntity app = applicationEntityMap.get(raDTO.getApplicationId());

            if (app != null) {
                client.updateRolePermissionMappings(
                    app.getName(),
                    roleName,
                    raDTO.getPermissions().toArray(String[]::new)
                );
            }
        }
    }
}
