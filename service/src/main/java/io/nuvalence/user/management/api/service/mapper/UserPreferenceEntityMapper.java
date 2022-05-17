package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.ApplicationPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationPreferenceDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Maps user preferences to a DTO.
 */
@Mapper(uses = LanguageEntityMapper.class)
public interface UserPreferenceEntityMapper {
    UserPreferenceEntityMapper INSTANCE = Mappers.getMapper(UserPreferenceEntityMapper.class);

    UserPreferenceDTO userPreferencesEntityToDto(UserPreferenceEntity preferences);

    @Mapping(target = "applicationPreferences", ignore = true)
    UserPreferenceEntity userPreferencesDtoToEntity(UserPreferenceDTO preferencesDto);

    @Mapping(target = "userPreferenceId", ignore = true)
    @Mapping(target = "application", ignore = true)
    ApplicationPreferenceEntity applicationPreferencesDtoToEntity(ApplicationPreferenceDTO applicationPreferencesDto);
}
