package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.cerbos.CerbosClient;
import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationPermissionEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleApplicationDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleUpdateRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.repository.ApplicationPermissionRepository;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RoleServiceTest {

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private ApplicationRepository applicationRepository;

    @Mock
    private ApplicationPermissionRepository applicationPermissionRepository;

    @Mock
    private PermissionRepository permissionRepository;

    @Mock
    private UserRoleRepository userRoleRepository;

    @Mock
    private CerbosClient client;

    @InjectMocks
    private RoleService roleService;

    @Captor
    private ArgumentCaptor<RoleEntity> roleCaptor;

    // Add role tests
    @Test
    public void addRole_addsRoleIfValid() {
        RoleCreationRequest roleCreationRequest = createRoleCreationRequest();
        when(client.updateRolePermissionMappings(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            any()))
            .thenReturn(true);

        RoleApplicationDTO roleApplicationDTO = createRoleApplicationDTO();
        roleCreationRequest.setApplications(List.of(roleApplicationDTO));
        ApplicationEntity applicationEntity = createApplicationEntity();
        applicationEntity.setId(UUID.fromString("ad00dbd6-f7dc-11ec-b939-0242ac120002"));
        List<UUID> appIds = List.of(roleApplicationDTO.getApplicationId());
        List<ApplicationEntity> applications = List.of(applicationEntity);
        when(applicationRepository.findAllById(appIds))
            .thenReturn(applications);
        PermissionEntity permissionEntity = createPermissionEntity();
        when(permissionRepository.findByPermissionName(roleApplicationDTO.getPermissions().get(0)))
            .thenReturn(Optional.of(permissionEntity));
        ApplicationPermissionEntity applicationPermissionEntity = createApplicationPermissionEntity(applicationEntity,
            permissionEntity);
        List<ApplicationPermissionEntity> applicationPermissionEntities =
            new ArrayList<>();
        applicationPermissionEntities.add(applicationPermissionEntity);
        when(applicationPermissionRepository.findAll())
            .thenReturn(applicationPermissionEntities);

        ResponseEntity<Void> res = roleService.addRole(roleCreationRequest);
        assertEquals(HttpStatus.OK, res.getStatusCode());
        verify(roleRepository).save(roleCaptor.capture());
        RoleEntity roleCaptured = roleCaptor.getValue();

        assertEquals(roleCreationRequest.getRoleName(), roleCaptured.getRoleName());
        assertEquals(roleCreationRequest.getDisplayName(), roleCaptured.getDisplayName());
    }

    @Test
    public void addRole_fails_ifRoleExists() {
        RoleCreationRequest role = createRoleCreationRequest();

        when(roleRepository.findByRoleName(role.getRoleName())).thenReturn(new RoleEntity());

        Exception exception = assertThrows(BusinessLogicException.class, () -> roleService.addRole(role));
        assertEquals("This role already exists.", exception.getMessage());
    }


    // Update role tests
    @Test
    public void updateRole_updatesRoleIfValid() {
        RoleEntity roleEntity = createRoleEntity();
        when(client.updateRolePermissionMappings(
            ArgumentMatchers.anyString(),
            ArgumentMatchers.anyString(),
            any()))
            .thenReturn(true);
        when(roleRepository.findById(roleEntity.getId())).thenReturn(Optional.of(createRoleEntity()));

        RoleApplicationDTO roleApplicationDTO = new RoleApplicationDTO();
        roleApplicationDTO.setApplicationId(UUID.fromString("ad00dbd6-f7dc-11ec-b939-0242ac120002"));
        roleApplicationDTO.setPermissions(List.of("Invalid_Permission"));
        RoleUpdateRequest roleUpdateRequest = new RoleUpdateRequest();
        roleUpdateRequest.setApplications(List.of(roleApplicationDTO));
        ApplicationEntity applicationEntity = createApplicationEntity();
        applicationEntity.setId(UUID.fromString("ad00dbd6-f7dc-11ec-b939-0242ac120002"));
        PermissionEntity permissionEntity = createPermissionEntity();
        List<UUID> appIds = List.of(roleApplicationDTO.getApplicationId());
        List<ApplicationEntity> applications = List.of(applicationEntity);
        when(applicationRepository.findAllById(appIds))
            .thenReturn(applications);
        when(permissionRepository.findByPermissionName(roleApplicationDTO.getPermissions().get(0)))
            .thenReturn(Optional.of(permissionEntity));
        ApplicationPermissionEntity applicationPermissionEntity = createApplicationPermissionEntity(applicationEntity,
            permissionEntity);
        List<ApplicationPermissionEntity> applicationPermissionEntities =
            new ArrayList<>();
        applicationPermissionEntities.add(applicationPermissionEntity);
        when(applicationPermissionRepository.findAll())
            .thenReturn(applicationPermissionEntities);

        ResponseEntity<Void> response = roleService.updateRole(roleEntity.getId(), roleUpdateRequest);
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    public void updateRole_fails_ifInvalidRoleId() {
        RoleEntity roleEntity = createRoleEntity();
        RoleUpdateRequest roleUpdateRequest = new RoleUpdateRequest();

        when(roleRepository.findById(roleEntity.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(BusinessLogicException.class, () ->
            roleService.updateRole(roleEntity.getId(), roleUpdateRequest));
        assertEquals("This role does not exist.", exception.getMessage());
    }

    @Test
    public void updateRole_fails_ifInvalidApplication() {
        RoleEntity roleEntity = createRoleEntity();
        RoleUpdateRequest roleUpdateRequest = new RoleUpdateRequest();

        when(roleRepository.findById(roleEntity.getId())).thenReturn(Optional.of(createRoleEntity()));

        RoleApplicationDTO roleApplicationDTO = new RoleApplicationDTO();
        roleApplicationDTO.setApplicationId(UUID.fromString("ad00dbd6-f7dc-11ec-b939-0242ac120002"));
        roleUpdateRequest.setApplications(List.of(roleApplicationDTO));

        Exception exception = assertThrows(BusinessLogicException.class, () ->
            roleService.updateRole(roleEntity.getId(), roleUpdateRequest));
        assertEquals(
            "The provided application id 'ad00dbd6-f7dc-11ec-b939-0242ac120002' is invalid.",
            exception.getMessage());
    }

    @Test
    public void updateRole_fails_ifInvalidPermission() {
        RoleEntity roleEntity = createRoleEntity();

        when(roleRepository.findById(roleEntity.getId())).thenReturn(Optional.of(createRoleEntity()));

        RoleApplicationDTO roleApplicationDTO = new RoleApplicationDTO();
        roleApplicationDTO.setApplicationId(UUID.fromString("ad00dbd6-f7dc-11ec-b939-0242ac120002"));
        roleApplicationDTO.setPermissions(List.of("Invalid_Role"));
        RoleUpdateRequest roleUpdateRequest = new RoleUpdateRequest();
        roleUpdateRequest.setApplications(List.of(roleApplicationDTO));
        ApplicationEntity applicationEntity = createApplicationEntity();
        applicationEntity.setId(UUID.fromString("ad00dbd6-f7dc-11ec-b939-0242ac120002"));
        List<UUID> appIds = List.of(roleApplicationDTO.getApplicationId());
        List<ApplicationEntity> applications = List.of(applicationEntity);
        when(applicationRepository.findAllById(appIds))
            .thenReturn(applications);
        Exception exception = assertThrows(BusinessLogicException.class, () ->
            roleService.updateRole(roleEntity.getId(), roleUpdateRequest));
        assertEquals("The provided permission 'Invalid_Role' is invalid.", exception.getMessage());
    }


    // Get all roles tests
    @Test
    public void getAllRoles_returnsAllRoles() {
        // Create two applications, one with 1 permission and one with 2 permissions
        ApplicationEntity a1 = createApplicationEntityAndPermissions(1, 1);
        ApplicationEntity a2 = createApplicationEntityAndPermissions(2, 2);

        // Add them to a new list for mocking the application repository
        List<ApplicationEntity> applicationEntities = new ArrayList<>();
        applicationEntities.add(a1);
        applicationEntities.add(a2);
        when(applicationRepository.findAll()).thenReturn(applicationEntities);

        // Create some roles to test with and for mocking the role repository
        ArrayList<RoleEntity> roleEntities = new ArrayList<>();
        roleEntities.add(createNumberedRoleEntity(1));
        roleEntities.add(createNumberedRoleEntity(2));
        roleEntities.add(createNumberedRoleEntity(3));
        when(roleRepository.findAll()).thenReturn(roleEntities);

        // Create the permission mappings for mocking the Cerbos client
        Map<String, String[]> app1PermissionMappings = new HashMap<>();
        String[] app1PermissionMappingsValue = {a1.getPermissions().get(0).getPermission().getName()};
        app1PermissionMappings.put(roleEntities.get(0).getRoleName(), app1PermissionMappingsValue);
        when(client.getRolePermissionMappings(a1.getName())).thenReturn(app1PermissionMappings);

        Map<String, String[]> app2PermissionMappings = new HashMap<>();
        String[] app2PermissionMappingsValue = {
            a2.getPermissions().get(0).getPermission().getName(),
            a2.getPermissions().get(1).getPermission().getName()
        };
        app2PermissionMappings.put(roleEntities.get(1).getRoleName(), app2PermissionMappingsValue);
        when(client.getRolePermissionMappings(a2.getName())).thenReturn(app2PermissionMappings);

        // Make the request
        ResponseEntity<List<RoleDTO>> rawRes = roleService.getAllRolesByResource(null);
        assertEquals(rawRes.getStatusCode(), HttpStatus.OK);

        // Validate main response structure
        List<RoleDTO> res = rawRes.getBody();
        assertNotNull(res);
        assertEquals(3, res.size());

        // Validate role 1
        assertEquals(roleEntities.get(0).getRoleName(), res.get(0).getRoleName());
        assertEquals(roleEntities.get(0).getDisplayName(), res.get(0).getDisplayName());
        assertEquals(1, res.get(0).getApplications().size());
        assertEquals(a1.getId(), res.get(0).getApplications().get(0).getApplicationId());
        assertEquals(a1.getName(), res.get(0).getApplications().get(0).getName());
        assertEquals(a1.getDisplayName(), res.get(0).getApplications().get(0).getDisplayName());
        assertEquals(1, res.get(0).getApplications().get(0).getPermissions().size());
        assertEquals(a1.getPermissions().get(0).getPermission().getName(),
            res.get(0).getApplications().get(0).getPermissions().get(0));

        // Validate role 2
        assertEquals(roleEntities.get(1).getRoleName(), res.get(1).getRoleName());
        assertEquals(roleEntities.get(1).getDisplayName(), res.get(1).getDisplayName());
        assertEquals(1, res.get(1).getApplications().size());
        assertEquals(a2.getId(), res.get(1).getApplications().get(0).getApplicationId());
        assertEquals(a2.getName(), res.get(1).getApplications().get(0).getName());
        assertEquals(a2.getDisplayName(), res.get(1).getApplications().get(0).getDisplayName());
        assertEquals(2, res.get(1).getApplications().get(0).getPermissions().size());
        assertEquals(a2.getPermissions().get(0).getPermission().getName(),
            res.get(1).getApplications().get(0).getPermissions().get(0));
        assertEquals(a2.getPermissions().get(1).getPermission().getName(),
            res.get(1).getApplications().get(0).getPermissions().get(1));

        // Validate role 3
        assertEquals(roleEntities.get(2).getRoleName(), res.get(2).getRoleName());
        assertEquals(roleEntities.get(2).getDisplayName(), res.get(2).getDisplayName());
        assertEquals(0, res.get(2).getApplications().size());
    }

    @Test
    public void getAllRolesByResource_returnsAllRoles() {
        // Create an application (with 1 permission) and set up mocking
        ApplicationEntity a1 = createApplicationEntityAndPermissions(1, 1);
        when(applicationRepository.getApplicationByName(a1.getName())).thenReturn(Optional.of(a1));

        // Create some roles to test with and for mocking the role repository
        ArrayList<RoleEntity> roleEntities = new ArrayList<>();
        roleEntities.add(createNumberedRoleEntity(1));
        roleEntities.add(createNumberedRoleEntity(2));
        when(roleRepository.findAll()).thenReturn(roleEntities);

        // Create the permission mappings for mocking the Cerbos client
        // Note: Only the first role is given a mapping since we want to test that both roles show up and that
        // only the correct application is shown for the correct role
        Map<String, String[]> app1PermissionMappings = new HashMap<>();
        String[] app1PermissionMappingsValue = {a1.getPermissions().get(0).getPermission().getName()};
        app1PermissionMappings.put(roleEntities.get(0).getRoleName(), app1PermissionMappingsValue);
        when(client.getRolePermissionMappings(a1.getName())).thenReturn(app1PermissionMappings);

        // Make the request
        ResponseEntity<List<RoleDTO>> rawRes = roleService.getAllRolesByResource(a1.getName());
        assertEquals(rawRes.getStatusCode(), HttpStatus.OK);

        // Validate main response structure
        List<RoleDTO> res = rawRes.getBody();
        assertNotNull(res);
        assertEquals(2, res.size());

        // Validate role 1
        assertEquals(roleEntities.get(0).getRoleName(), res.get(0).getRoleName());
        assertEquals(roleEntities.get(0).getDisplayName(), res.get(0).getDisplayName());
        assertEquals(1, res.get(0).getApplications().size());
        assertEquals(a1.getId(), res.get(0).getApplications().get(0).getApplicationId());
        assertEquals(a1.getName(), res.get(0).getApplications().get(0).getName());
        assertEquals(a1.getDisplayName(), res.get(0).getApplications().get(0).getDisplayName());
        assertEquals(1, res.get(0).getApplications().get(0).getPermissions().size());
        assertEquals(a1.getPermissions().get(0).getPermission().getName(),
            res.get(0).getApplications().get(0).getPermissions().get(0));

        // Validate role 2
        assertEquals(roleEntities.get(1).getRoleName(), res.get(1).getRoleName());
        assertEquals(roleEntities.get(1).getDisplayName(), res.get(1).getDisplayName());
        assertEquals(0, res.get(1).getApplications().size());
    }


    // Delete role tests
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


    // Get users by role ID tests
    @Test
    public void getUsersByRoleId_returnsUsersIfValid() {
        RoleEntity role = createRoleEntity();
        List<UserRoleEntity> userRoleEntities = createUserRoleList();

        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
        when(userRoleRepository.findAllByRoleId(role.getId())).thenReturn(userRoleEntities);

        ResponseEntity<List<UserDTO>> res = roleService.getUsersByRoleId(role.getId());
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(Objects.requireNonNull(res.getBody()).size(), 2);

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
        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
            roleService.getUsersByRoleId(createRoleEntity().getId()));
        assertEquals(exception.getMessage(), "There is no role with this id");
    }

    @Test
    public void getUsersByRoleId_fails_ifNoUserHasThisRole() {
        RoleEntity role = createRoleEntity();
        String resourceName = "default_resource";
        when(roleRepository.findById(role.getId())).thenReturn(Optional.of(role));
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setName(resourceName);

        Exception exception = assertThrows(BusinessLogicException.class, () ->
            roleService.getUsersByRoleId(role.getId()));
        assertEquals("There are no users with this role", exception.getMessage());
    }

    // Helpers

    private RoleDTO createRoleDto() {
        RoleDTO role = new RoleDTO();
        role.setRoleName("ROLE_TO_TEST");
        role.setDisplayName("Role To Test");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        role.setApplications(new ArrayList<>());
        return role;
    }

    private RoleEntity createRoleEntity() {
        RoleEntity role = new RoleEntity();
        role.setRoleName("ROLE_TO_TEST");
        role.setDisplayName("Role To Test");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        return role;
    }

    private RoleEntity createNumberedRoleEntity(int num) {
        RoleEntity role = new RoleEntity();
        role.setRoleName("role" + num);
        role.setDisplayName("Role " + num);
        role.setId(UUID.randomUUID());
        return role;
    }

    private RoleCreationRequest createRoleCreationRequest() {
        RoleCreationRequest role = new RoleCreationRequest();
        role.setRoleName("COMPLAINANT");
        role.setDisplayName("Complainant");
        RoleApplicationDTO raDTO = new RoleApplicationDTO();
        raDTO.setPermissions(List.of("TransactionManager_viewAll"));
        role.setApplications(List.of(raDTO));
        return role;
    }

    private List<UserRoleEntity> createUserRoleList() {
        UserEntity user0 = new UserEntity();
        user0.setId(UUID.fromString("ca8cfd1b-8643-4185-ba7f-8c8fbc9a7da6"));
        user0.setEmail("MrIncognito@Illuminati.org");
        user0.setDisplayName("Rodney Safe-Valley");
        UserEntity user1 = new UserEntity();
        user1.setId(UUID.fromString("ca8cfd1b-1337-4185-ba7f-8c8fbc9a7da6"));
        user1.setEmail("iUseArchBtw@thunderbird.com");
        user1.setDisplayName("It doesn't compile");
        UserRoleEntity userRoleEntity = createUserRoleEntity(user0);
        UserRoleEntity userRoleEntity1 = createUserRoleEntity(user1);

        return List.of(userRoleEntity, userRoleEntity1);
    }

    private UserRoleEntity createUserRoleEntity(UserEntity user) {
        UserRoleEntity userRoleEntity = new UserRoleEntity();
        userRoleEntity.setUser(user);
        userRoleEntity.setRole(createRoleEntity());
        return userRoleEntity;
    }

    private RoleApplicationDTO createRoleApplicationDTO() {
        RoleApplicationDTO roleApplicationDTO = new RoleApplicationDTO();
        roleApplicationDTO.setApplicationId(UUID.fromString("ad00dbd6-f7dc-11ec-b939-0242ac120002"));
        roleApplicationDTO.setPermissions(List.of("Valid_Permission"));
        return roleApplicationDTO;
    }

    private PermissionEntity createPermissionEntity() {
        PermissionEntity entity = new PermissionEntity();
        entity.setName("Test Permission");
        entity.setDisplayName("Test Permission");
        entity.setId(UUID.randomUUID());
        return entity;
    }

    private ApplicationEntity createApplicationEntity() {
        ApplicationEntity entity = new ApplicationEntity();
        entity.setName("test_application");
        entity.setDisplayName("Test Application");
        entity.setId(UUID.randomUUID());
        return entity;
    }

    private ApplicationEntity createApplicationEntityAndPermissions(int appNumber, int numPermissions) {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(UUID.randomUUID());
        applicationEntity.setName("app" + appNumber);
        applicationEntity.setDisplayName("Application " + appNumber);
        ArrayList<ApplicationPermissionEntity> aps = new ArrayList<>();
        for (int i = 1; i < numPermissions + 1; i++) {
            PermissionEntity permissionEntity = new PermissionEntity();
            permissionEntity.setId(UUID.randomUUID());
            permissionEntity.setName("app" + appNumber + "permission" + i);
            permissionEntity.setDisplayName("Application " + appNumber + " Permission " + i);
            aps.add(createApplicationPermissionEntity(applicationEntity, permissionEntity));
        }
        applicationEntity.setPermissions(aps);
        return applicationEntity;
    }

    private ApplicationPermissionEntity createApplicationPermissionEntity(ApplicationEntity application,
                                                                          PermissionEntity permission) {
        ApplicationPermissionEntity entity = new ApplicationPermissionEntity();
        entity.setId(UUID.randomUUID());
        entity.setApplication(application);
        entity.setPermission(permission);
        return entity;
    }
}
