package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.generated.models.AssignedRoleDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Maps transaction definitions between RoleEntity and Role.
 */

@Mapper
public interface RoleEntityMapper {

    RoleEntityMapper INSTANCE = Mappers.getMapper(RoleEntityMapper.class);

    /**
     * Converts a Role Entity into a Role DTO.
     *
     * @param roleEntity Role in Entity form
     * @return Role DTO
     */
    @Mapping(target = "applications", ignore = true)
    RoleDTO roleEntityToRoleDto(RoleEntity roleEntity);

    /**
     * Converts a Role Entity into an Assigned Role DTO.
     *
     * @param roleEntity Role in Entity form
     * @return Assigned Role DTO
     */
    AssignedRoleDTO roleEntityToAssignedRoleDto(RoleEntity roleEntity);

    /**
     * Converts a Role Entity into a Role DTO.
     *
     * @param role Role in DTO form
     * @return Role Entity.
     */
    @Mapping(target = "userRoleEntities", ignore = true)
    RoleEntity roleDtoToRoleEntity(RoleDTO role);

    /**
     * Converts a Role Creation Request into a Role DTO.
     *
     * @param role Role in RoleCreationRequest form
     * @return Role Entity.
     */
    RoleEntity roleCreationRequestToRoleEntity(RoleCreationRequest role);
}
