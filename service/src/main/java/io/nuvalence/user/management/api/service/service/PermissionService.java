package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationPermissionEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.mapper.PermissionEntityMapper;
import io.nuvalence.user.management.api.service.repository.ApplicationPermissionRepository;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

/**
 * Service for Permission.
 */

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class PermissionService {

    private final PermissionRepository permissionRepository;
    private final ApplicationPermissionRepository applicationPermissionRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * Adds a permission to the database.
     *
     * @param permission the permission to add.
     * @return a status code.
     */
    public ResponseEntity<Void> addPermission(CreateOrUpdatePermissionDTO permission) {
        Optional<PermissionEntity> nameCheck = permissionRepository
                .findByPermissionName(permission.getName().toLowerCase());
        if (!nameCheck.isEmpty()) {
            throw new BusinessLogicException("This permission already exists.");
        }

        final PermissionEntity newPermission = PermissionEntityMapper.INSTANCE
                .permissionDtoToPermissionEntity(permission);
        // Ensures lowercase on submission
        newPermission.setName(permission.getName().toLowerCase());
        PermissionEntity savedPermission = permissionRepository.save(newPermission);

        if (permission.getApplications() != null && !permission.getApplications().isEmpty()) {
            List<ApplicationEntity> applications = applicationRepository
                    .findAllById(permission.getApplications());
            List<UUID> foundApplications = applications.stream().map(ApplicationEntity::getId)
                    .collect(Collectors.toList());
            Optional<UUID> notFoundApplication = permission.getApplications().stream()
                    .filter(a -> !foundApplications.contains(a))
                    .findFirst();
            if (notFoundApplication.isPresent()) {
                throw new BusinessLogicException(String.format("No application found with id: %s",
                        notFoundApplication.get()));
            }

            List<ApplicationPermissionEntity> applicationPermissions = applications.stream().map(a -> {
                ApplicationPermissionEntity newAppPerm = new ApplicationPermissionEntity();
                newAppPerm.setPermission(savedPermission);
                newAppPerm.setApplication(a);
                return newAppPerm;
            }).collect(Collectors.toList());
            applicationPermissionRepository.saveAll(applicationPermissions);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Updates a permission.
     *
     * @param permissionId The id of the permission.
     * @param permission The permission object.
     * @return a status code.
     */
    public ResponseEntity<Void> updatePermission(UUID permissionId, CreateOrUpdatePermissionDTO permission) {
        Optional<PermissionEntity> permissionEntity = permissionRepository.findById(permissionId);
        if (permissionEntity.isEmpty()) {
            throw new BusinessLogicException("This permission does not exist.");
        }

        permissionEntity.get().setName(permission.getName());
        permissionEntity.get().setDisplayName(permission.getDisplayName());
        permissionEntity.get().setDescription(permission.getDescription());
        PermissionEntity savedPermissionEntity = permissionRepository.save(permissionEntity.get());

        boolean currentlyHasApps = permissionEntity.get().getApplicationPermissionEntities() != null
                && !permissionEntity.get().getApplicationPermissionEntities().isEmpty();
        boolean willHaveApps = permission.getApplications() != null && !permission.getApplications().isEmpty();

        List<ApplicationPermissionEntity> appPermsToAdd = Collections.emptyList();
        List<ApplicationPermissionEntity> appPermsToRemove = Collections.emptyList();

        if (willHaveApps) {
            final Map<UUID, ApplicationEntity> permissionApplications = applicationRepository
                    .findAllById(permission.getApplications()).stream()
                    .collect(Collectors.toMap(ApplicationEntity::getId, a -> a));
            final List<UUID> currentAppIDs = currentlyHasApps
                    ? permissionEntity.get().getApplicationPermissionEntities().stream()
                    .map(a -> a.getApplication().getId())
                    .collect(Collectors.toList())
                    : Collections.emptyList();

            appPermsToAdd = permission.getApplications().stream().filter(a -> !currentAppIDs.contains(a)).map(a -> {
                ApplicationEntity application = permissionApplications.get(a);
                if (application == null) {
                    throw new BusinessLogicException(String.format("No application found with id: %s",
                            a.toString()));
                }

                ApplicationPermissionEntity newAppPerm = new ApplicationPermissionEntity();
                newAppPerm.setPermission(savedPermissionEntity);
                newAppPerm.setApplication(application);
                return newAppPerm;
            }).collect(Collectors.toList());
        }

        if (currentlyHasApps) {
            appPermsToRemove = permissionEntity.get().getApplicationPermissionEntities().stream()
                    .filter(a -> !willHaveApps || !permission.getApplications().contains(a.getApplication().getId()))
                    .collect(Collectors.toList());
        }

        applicationPermissionRepository.saveAll(appPermsToAdd);
        applicationPermissionRepository.deleteAll(appPermsToRemove);

        return ResponseEntity.ok().build();
    }

    /**
     * Fetches a list of all permissions that exist.
     *
     * @return a list of all permissions.
     */
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        List<PermissionEntity> permissionEntities = permissionRepository.findAll();

        List<PermissionDTO> permissions = MapperUtils.mapPermissionEntitiesToPermissionList(permissionEntities);

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(permissions);
    }

    /**
     * Deletes a permission by its id.
     *
     * @param permissionId is an id of a permission.
     * @return a status code.
     */
    public ResponseEntity<Void> deletePermissionById(UUID permissionId) {
        Optional<PermissionEntity> permissionEntity = permissionRepository.findById(permissionId);
        if (permissionEntity.isEmpty()) {
            throw new ResourceNotFoundException("There is no permission that exists with this id.");
        }

        List<ApplicationPermissionEntity> applicationPermissionEntities = applicationPermissionRepository
                .findByPermissionId(permissionId);

        applicationPermissionRepository.deleteAll(applicationPermissionEntities);
        permissionRepository.delete(permissionEntity.get());

        return ResponseEntity.ok().build();
    }

    /**
     * Returns a permission by its id.
     *
     * @param permissionId is a permission's id
     * @return a status code and a permission.
     */
    public ResponseEntity<PermissionDTO> getPermissionById(UUID permissionId) {
        Optional<PermissionEntity> permissionEntity = permissionRepository.findById(permissionId);
        if (permissionEntity.isEmpty()) {
            throw new ResourceNotFoundException("There is no permission that exists with this id.");
        }

        PermissionDTO permission = PermissionEntityMapper.INSTANCE
                .permissionEntityToPermissionDto(permissionEntity.get());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(permission);
    }
}