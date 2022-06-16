package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.ApplicationPermissionEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.SimpleListDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Maps between PermissionEntity and Permission.
 */

@Mapper
public interface PermissionEntityMapper {

    PermissionEntityMapper INSTANCE = Mappers.getMapper(PermissionEntityMapper.class);

    /**
     * Converts a {@link PermissionEntity}
     * to {@link io.nuvalence.ds4g.user.api.service.generated.models.PermissionDTO}.
     *
     * @param permissionEntity Permission in Entity form
     * @return Permission DTO
     */
    @Mapping(target = "applications", source = "permissionEntity.applicationPermissionEntities")
    PermissionDTO permissionEntityToPermissionDto(PermissionEntity permissionEntity);

    /**
     * Converts a {@link io.nuvalence.ds4g.user.api.service.generated.models.PermissionDTO} to
     * {@link PermissionEntity}.
     *
     * @param permission Permission in DTO form
     * @return Permission Entity
     */
    @Mapping(target = "applicationPermissionEntities", ignore = true)
    PermissionEntity permissionDtoToPermissionEntity(PermissionDTO permission);

    /**
     * Converts a {@link io.nuvalence.ds4g.user.api.service.generated.models.CreateOrUpdatePermissionDTO} to
     * {@link PermissionEntity}.
     *
     * @param permission Permission in DTO form
     * @return Permission Entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "applicationPermissionEntities", ignore = true)
    PermissionEntity permissionDtoToPermissionEntity(CreateOrUpdatePermissionDTO permission);

    /**
     * Converts a {@link ApplicationPermissionEntity} to
     * {@link io.nuvalence.ds4g.user.api.service.generated.models.PermissionDTO}.
     *
     * @param entity an entity
     * @return Permission entity
     */
    @Mapping(target = ".", source = "entity.permission")
    @Mapping(target = "applications", ignore = true)
    PermissionDTO applicationPermissionEntityToPermissionDto(ApplicationPermissionEntity entity);

    /**
     * Converts a {@link ApplicationPermissionEntity} to
     * {@link io.nuvalence.ds4g.user.api.service.generated.models.SimpleListDTO}.
     *
     * @param entity an entity
     * @return a simple list DTO
     */
    @Mapping(target = ".", source = "entity.application")
    SimpleListDTO applicationPermissionEntityToSimpleListDto(ApplicationPermissionEntity entity);
}