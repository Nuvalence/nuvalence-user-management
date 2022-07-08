package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.generated.models.LanguageDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.mapstruct.factory.Mappers;

/**
 * Utilities for mapping a Language entities to Language DTOs.
 */
@Mapper(unmappedSourcePolicy = ReportingPolicy.IGNORE)
public interface LanguageEntityMapper {
    LanguageEntityMapper INSTANCE = Mappers.getMapper(LanguageEntityMapper.class);

    /**
     * Maps a Language entity to a Language DTO.
     *
     * @param language Language as an entity.
     * @return Language DTO.
     */
    LanguageDTO languageEntityToLanguageDto(LanguageEntity language);

    @Mapping(target = "applicationLanguages", ignore = true)
    LanguageEntity languageDtoToLanguageEntity(LanguageDTO languageDto);
}
