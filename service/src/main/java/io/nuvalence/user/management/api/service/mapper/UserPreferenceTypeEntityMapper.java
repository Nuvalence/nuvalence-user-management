package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.UserPreferenceTypeEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceTypeDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps user preference types to a DTO.
 */
@Mapper(uses = UserPreferenceTypeEntity.class)
public interface UserPreferenceTypeEntityMapper {
    UserPreferenceTypeEntityMapper INSTANCE = Mappers.getMapper(UserPreferenceTypeEntityMapper.class);

    UserPreferenceTypeDTO userPreferenceTypeEntityToDto(UserPreferenceTypeEntity preferenceType);

    UserPreferenceTypeEntity userPreferenceTypeDtoToEntity(UserPreferenceTypeDTO preferenceTypeDto);
}
