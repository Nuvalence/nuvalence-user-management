package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationPermissionEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
public class PermissionEntityMapperTest {

    @Test
    public void shouldMapPermissionEntityToPermissionDto() {
        PermissionEntity permission = new PermissionEntity();
        permission.setId(UUID.randomUUID());
        permission.setName("test_perm");
        permission.setDisplayName("Test Permission");
        permission.setDescription("This is a test permission.");

        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());
        application.setName("APPLICATION_1");

        ApplicationPermissionEntity applicationPermission = new ApplicationPermissionEntity();
        applicationPermission.setId(UUID.randomUUID());
        applicationPermission.setApplication(application);
        applicationPermission.setPermission(permission);

        permission.setApplicationPermissionEntities(List.of(applicationPermission));

        PermissionDTO permissionModel = PermissionEntityMapper.INSTANCE.permissionEntityToPermissionDto(permission);
        assertTrue(permission.getName().equalsIgnoreCase(permissionModel.getName()));
        assertEquals(permission.getDisplayName(), permissionModel.getDisplayName());
        assertEquals(permission.getDescription(), permissionModel.getDescription());
        assertEquals(permission.getApplicationPermissionEntities().size(), permissionModel.getApplications().size());
        assertEquals(
                permission.getApplicationPermissionEntities().get(0).getApplication().getId(),
                permissionModel.getApplications().get(0).getId()
        );
        assertTrue(
                permission.getApplicationPermissionEntities().get(0).getApplication().getName().equalsIgnoreCase(
                        permissionModel.getApplications().get(0).getName()
                )
        );
    }
}