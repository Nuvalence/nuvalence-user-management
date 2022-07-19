package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationPermissionEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.service.repository.ApplicationPermissionRepository;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PermissionServiceTest {
    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private ApplicationPermissionRepository applicationPermissionRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Captor
    private ArgumentCaptor<PermissionEntity> permissionCaptor;

    @Captor
    private ArgumentCaptor<UUID> idCaptor;

    @Captor
    private ArgumentCaptor<Iterable<ApplicationPermissionEntity>> applicationPermissionCaptor;

    @InjectMocks
    private PermissionService permissionService;

    @Test
    public void addPermission_creates_a_permission() {
        ApplicationEntity application = createApplicationEntity();
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        permissionModel.setApplications(List.of(application.getId()));

        when(applicationRepository.findAllById(any())).thenReturn(List.of(application));

        ResponseEntity<Void> res = permissionService.addPermission(permissionModel);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(permissionRepository).save(permissionCaptor.capture());
        PermissionEntity savedPermission = permissionCaptor.getValue();
        assertTrue(savedPermission.getName().equalsIgnoreCase(permissionModel.getName()));
        assertEquals(savedPermission.getDisplayName(), permissionModel.getDisplayName());
        assertEquals(savedPermission.getDescription(), permissionModel.getDescription());
        verify(applicationPermissionRepository).saveAll(applicationPermissionCaptor.capture());
        Iterable<ApplicationPermissionEntity> savedApplications = applicationPermissionCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(savedApplications), permissionModel.getApplications().size());
    }

    @Test
    public void addPermission_fails_ifNameIsTaken() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        PermissionEntity permissionEntity = createPermissionEntity();

        when(permissionRepository.findByPermissionName(permissionModel.getName()))
                .thenReturn(Optional.of(permissionEntity));

        Exception exception = assertThrows(BusinessLogicException.class, () ->
                permissionService.addPermission(permissionModel));
        assertTrue(exception.getMessage().contains("This permission already exists."));
    }

    @Test
    public void addPermission_fails_ifApplicationDoesNotExist() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();

        when(applicationRepository.findAllById(any())).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(BusinessLogicException.class, () ->
                permissionService.addPermission(permissionModel));
        assertTrue(exception.getMessage().contains(String.format("No application found with id: %s",
                permissionModel.getApplications().get(0).toString())));
    }

    @Test
    public void updatePermission_updates_a_permission() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        PermissionEntity permission = createPermissionEntity();
        permission.setApplicationPermissionEntities(List.of(createApplicationPermissionEntity(permission)));

        // make the model app list the same as what the entity already has associated to it
        permissionModel.setApplications(
                permission.getApplicationPermissionEntities()
                        .stream().map(a -> a.getApplication().getId()).collect(Collectors.toList())
        );

        when(permissionRepository.findById(any())).thenReturn(Optional.of(permission));
        when(applicationRepository.findAllById(any())).thenReturn(
                permission.getApplicationPermissionEntities().stream()
                        .map(ApplicationPermissionEntity::getApplication).collect(Collectors.toList())
        );

        ResponseEntity<Void> res = permissionService.updatePermission(UUID.randomUUID(), permissionModel);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(permissionRepository).save(permissionCaptor.capture());
        PermissionEntity savedPermission = permissionCaptor.getValue();
        assertTrue(savedPermission.getName().equalsIgnoreCase(permissionModel.getName()));
        assertEquals(savedPermission.getDisplayName(), permissionModel.getDisplayName());
        assertEquals(savedPermission.getDescription(), permissionModel.getDescription());
        assertEquals(savedPermission.getApplicationPermissionEntities().size(),
                permissionModel.getApplications().size());

        verify(applicationPermissionRepository).saveAll(applicationPermissionCaptor.capture());
        Iterable<ApplicationPermissionEntity> addedAppPerms = applicationPermissionCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(addedAppPerms), 0);

        verify(applicationPermissionRepository).deleteAll(applicationPermissionCaptor.capture());
        Iterable<ApplicationPermissionEntity> deletedAppPerms = applicationPermissionCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(deletedAppPerms), 0);
    }

    @Test
    public void updatePermission_updates_a_permission_and_adds_removes_applications() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        // make the model app list different from what's there to force both an insert and a deletion to happen
        permissionModel.setApplications(List.of(UUID.randomUUID()));
        PermissionEntity permission = createPermissionEntity();
        permission.setApplicationPermissionEntities(List.of(createApplicationPermissionEntity(permission)));
        ApplicationEntity applicationEntity = createApplicationEntity();
        applicationEntity.setId(permissionModel.getApplications().get(0));

        when(permissionRepository.findById(any())).thenReturn(Optional.of(permission));
        when(applicationRepository.findAllById(any())).thenReturn(List.of(applicationEntity));

        ResponseEntity<Void> res = permissionService.updatePermission(UUID.randomUUID(), permissionModel);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(permissionRepository).save(permissionCaptor.capture());
        PermissionEntity savedPermission = permissionCaptor.getValue();
        assertTrue(savedPermission.getName().equalsIgnoreCase(permissionModel.getName()));
        assertEquals(savedPermission.getDisplayName(), permissionModel.getDisplayName());
        assertEquals(savedPermission.getDescription(), permissionModel.getDescription());
        assertEquals(savedPermission.getApplicationPermissionEntities().size(),
                permissionModel.getApplications().size());

        verify(applicationPermissionRepository).saveAll(applicationPermissionCaptor.capture());
        Iterable<ApplicationPermissionEntity> addedAppPerms = applicationPermissionCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(addedAppPerms), 1);

        verify(applicationPermissionRepository).deleteAll(applicationPermissionCaptor.capture());
        Iterable<ApplicationPermissionEntity> deletedAppPerms = applicationPermissionCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(deletedAppPerms), 1);
    }

    @Test
    public void updatePermission_fails_if_permissionDoesNotExist() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        when(permissionRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(BusinessLogicException.class, () ->
                permissionService.updatePermission(UUID.randomUUID(), permissionModel));
        assertTrue(exception.getMessage().contains("This permission does not exist."));
    }

    @Test
    public void updatePermission_fails_if_applicationDoesNotExist() {
        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionRequest();
        PermissionEntity permissionEntity = createPermissionEntity();

        when(permissionRepository.findById(any())).thenReturn(Optional.of(permissionEntity));
        when(applicationRepository.findAllById(any())).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(BusinessLogicException.class, () ->
                permissionService.updatePermission(UUID.randomUUID(), permissionModel));
        assertTrue(exception.getMessage().contains(String.format("No application found with id: %s",
                permissionModel.getApplications().get(0).toString())));
    }

    @Test
    public void getAllPermissions_gets_permissions() {
        PermissionEntity permissionEntity = createPermissionEntity();
        permissionEntity.setApplicationPermissionEntities(List.of(createApplicationPermissionEntity(permissionEntity)));
        when(permissionRepository.findAll()).thenReturn(List.of(permissionEntity));

        ResponseEntity<List<PermissionDTO>> res = permissionService.getAllPermissions();

        assertEquals(HttpStatus.OK, res.getStatusCode());
        assertEquals(1, Objects.requireNonNull(res.getBody()).size());
        assertEquals(permissionEntity.getName(), res.getBody().get(0).getName());
        assertEquals(permissionEntity.getDisplayName(), res.getBody().get(0).getDisplayName());
        assertEquals(permissionEntity.getDescription(), res.getBody().get(0).getDescription());
        assertEquals(permissionEntity.getApplicationPermissionEntities().get(0).getApplication().getName(),
            res.getBody().get(0).getApplications().get(0).getName());
        assertEquals(permissionEntity.getApplicationPermissionEntities().get(0).getApplication().getDisplayName(),
            res.getBody().get(0).getApplications().get(0).getDisplayName());
        verify(permissionRepository).findAll();
    }

    @Test
    public void deletePermissionById_deletes_permission() {
        PermissionEntity permissionEntity = createPermissionEntity();
        permissionEntity.setApplicationPermissionEntities(List.of(createApplicationPermissionEntity(permissionEntity)));
        when(permissionRepository.findById(any())).thenReturn(Optional.of(permissionEntity));

        ResponseEntity<Void> res = permissionService.deletePermissionById(UUID.randomUUID());
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(applicationPermissionRepository).deleteAll(applicationPermissionCaptor.capture());
        verify(permissionRepository).delete(permissionCaptor.capture());
    }

    @Test
    public void deletePermissionById_fails_if_permissionDoesNotExist() {
        when(permissionRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                permissionService.deletePermissionById(UUID.randomUUID()));
        assertTrue(exception.getMessage().contains("There is no permission that exists with this id."));
    }

    @Test
    public void getPermissionById_gets_permission() {
        PermissionEntity permissionEntity = createPermissionEntity();
        permissionEntity.setApplicationPermissionEntities(List.of(createApplicationPermissionEntity(permissionEntity)));
        when(permissionRepository.findById(any())).thenReturn(Optional.of(permissionEntity));

        ResponseEntity<PermissionDTO> res = permissionService.getPermissionById(permissionEntity.getId());
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(permissionRepository).findById(idCaptor.capture());
        assertEquals(Objects.requireNonNull(res.getBody()).getId(), idCaptor.getValue());
    }

    @Test
    public void getPermissionById_fails_if_permissionDoesNotExist() {
        when(permissionRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                permissionService.getPermissionById(UUID.randomUUID()));
        assertTrue(exception.getMessage().contains("There is no permission that exists with this id."));
    }

    private PermissionEntity createPermissionEntity() {
        PermissionEntity permissionEntity = new PermissionEntity();
        permissionEntity.setId(UUID.randomUUID());
        permissionEntity.setName("test_perm");
        permissionEntity.setDisplayName("Test Permission");
        permissionEntity.setDescription("This is a test permission.");
        return permissionEntity;
    }

    private CreateOrUpdatePermissionDTO createOrUpdatePermissionRequest() {
        CreateOrUpdatePermissionDTO permission = new CreateOrUpdatePermissionDTO();
        permission.setName("test_perm");
        permission.setDisplayName("Test Permission");
        permission.setDescription("This is a test permission.");
        permission.setApplications(List.of(UUID.randomUUID()));
        return permission;
    }

    private ApplicationPermissionEntity createApplicationPermissionEntity(PermissionEntity permissionEntity) {
        ApplicationPermissionEntity applicationPermissionEntity = new ApplicationPermissionEntity();
        applicationPermissionEntity.setPermission(permissionEntity);
        applicationPermissionEntity.setApplication(createApplicationEntity());
        applicationPermissionEntity.setId(UUID.randomUUID());
        return applicationPermissionEntity;
    }

    private ApplicationEntity createApplicationEntity() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(UUID.randomUUID());
        applicationEntity.setName("APPLICATION_1");
        return applicationEntity;
    }
}