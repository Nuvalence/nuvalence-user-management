package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class RoleEntityMapperTest {

    @Test
    public void shouldMapRoleEntityToRoleDto() {
        RoleEntity role = new RoleEntity();
        role.setRoleName("ROLE_TO_TEST");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));

        RoleDTO roleModel = RoleEntityMapper.INSTANCE.roleEntityToRoleDto(role);
        assertEquals(role.getRoleName(), roleModel.getRoleName());
        assertEquals(role.getId(), roleModel.getId());
    }

    @Test
    public void shouldMapRoleDtoToRoleEntity() {
        RoleDTO role = new RoleDTO();
        role.setRoleName("ROLE_TO_TEST");
        role.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));

        RoleEntity roleEntity = RoleEntityMapper.INSTANCE.roleDtoToRoleEntity(role);
        assertEquals(role.getRoleName(), roleEntity.getRoleName());
        assertEquals(role.getId(), roleEntity.getId());
    }

}
