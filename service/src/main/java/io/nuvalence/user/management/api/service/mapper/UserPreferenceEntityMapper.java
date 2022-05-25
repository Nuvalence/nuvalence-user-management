package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * Maps user preferences to a DTO.
 */
@Mapper(uses = LanguageEntityMapper.class)
public interface UserPreferenceEntityMapper {
    UserPreferenceEntityMapper INSTANCE = Mappers.getMapper(UserPreferenceEntityMapper.class);

    UserPreferenceDTO userPreferencesEntityToDto(UserPreferenceEntity preferences);

    UserPreferenceEntity userPreferencesDtoToEntity(UserPreferenceDTO preferencesDto);
}
