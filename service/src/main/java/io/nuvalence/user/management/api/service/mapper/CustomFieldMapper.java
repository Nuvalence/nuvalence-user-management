package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.CustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldOptionEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateCustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldOptionDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

/**
 * Maps between CustomFieldEntity and CustomFieldDTO.
 */

@Mapper
public interface CustomFieldMapper {
    CustomFieldMapper INSTANCE = Mappers.getMapper(CustomFieldMapper.class);

    /**
     * Maps {@link CustomFieldEntity} to
     * {@link CustomFieldDTO}.
     *
     * @param entity a custom field entity
     * @return custom field model
     */
    @Mapping(source = "entity.type.type", target = "type")
    @Mapping(source = "entity.dataType.type", target = "dataType")
    CustomFieldDTO convertEntityToDto(CustomFieldEntity entity);

    /**
     * Maps {@link CustomFieldEntity} to
     * {@link CustomFieldOptionDTO}.
     *
     * @param entity a custom field option entity
     * @return custom field option model
     */
    CustomFieldOptionDTO convertOptionEntityToOptionDto(CustomFieldOptionEntity entity);

    /**
     * Maps {@link CustomFieldOptionDTO} to
     * {@link CustomFieldOptionEntity}.
     *
     * @param dto a custom field option DTO
     * @param customField the custom field to associate the option to
     * @return a custom field option entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "displayText", source = "dto.displayText")
    CustomFieldOptionEntity convertOptionDtoToOptionEntity(CustomFieldOptionDTO dto, CustomFieldEntity customField);

    /**
     * Maps {@link CreateOrUpdateCustomFieldOptionDTO} to
     * {@link CustomFieldOptionEntity}.
     *
     * @param dto a custom field option DTO
     * @param customField the custom field to associate the option to
     * @return a custom field option entity
     */
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "displayText", source = "dto.displayText")
    CustomFieldOptionEntity convertOptionDtoToOptionEntity(CreateOrUpdateCustomFieldOptionDTO dto,
                                                           CustomFieldEntity customField);
}
