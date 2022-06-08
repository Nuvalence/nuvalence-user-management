package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Maps transaction definitions between UserEntity and User.
 */

@Mapper
public interface UserEntityMapper {
    UserEntityMapper INSTANCE = Mappers.getMapper(UserEntityMapper.class);

    /**
     * Maps {@link UserEntity} to
     * {@link UserDTO}.
     *
     * @param userEntity an entity
     * @return user model
     */
    @Mapping(target = "preferences", ignore = true)
    @Mapping(target = "assignedRoles", ignore = true)
    UserDTO convertUserEntityToUserModel(UserEntity userEntity);

    /**
     * Maps {@link UserDTO} to
     * {@link UserEntity}.
     *
     * @param user is a model
     * @return an entity.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "userRoleEntities", ignore = true)
    @Mapping(target = "customFields", ignore = true)
    UserEntity convertUserModelToUserEntity(UserDTO user);

    /**
     * Maps {@link UserCreationRequest} to
     * {@link UserEntity}.
     *
     * @param user is user creation request DTO
     * @return a user entity.
     */
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "userRoleEntities", ignore = true)
    @Mapping(target = "customFields", ignore = true)
    @Mapping(target = "id", ignore = true)
    UserEntity convertUserCreationRequestToUserEntity(UserCreationRequest user);
}
