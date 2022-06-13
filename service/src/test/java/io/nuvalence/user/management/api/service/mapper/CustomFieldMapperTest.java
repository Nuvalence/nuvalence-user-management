package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.CustomFieldDataTypeEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldOptionEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldTypeEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateCustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class CustomFieldMapperTest {

    @Test
    public void shouldMapCustomFieldEntityToCustomFieldDto() {
        CustomFieldEntity customFieldEntity = getCustomFieldEntity();
        CustomFieldDTO customFieldModel = CustomFieldMapper.INSTANCE.convertEntityToDto(customFieldEntity);
        assertEquals(customFieldEntity.getId(), customFieldModel.getId());
        assertEquals(customFieldEntity.getName(), customFieldModel.getName());
        assertEquals(customFieldEntity.getDisplayText(), customFieldModel.getDisplayText());
        assertEquals(customFieldEntity.getType().getType(), customFieldModel.getType().getValue());
        assertEquals(customFieldEntity.getDataType().getType(), customFieldModel.getDataType().getValue());
        assertEquals(customFieldEntity.getOptions().get(0).getDisplayText(),
                customFieldModel.getOptions().get(0).getDisplayText());
        assertEquals(customFieldEntity.getOptions().get(0).getOptionValue(),
                customFieldModel.getOptions().get(0).getOptionValue());
    }

    @Test
    public void shouldMapCreateOrUpdateCustomFieldOptionDtoToCustomFieldOptionEntity() {
        CreateOrUpdateCustomFieldOptionDTO option = new CreateOrUpdateCustomFieldOptionDTO();
        option.setOptionValue("VALUE_1");
        option.setDisplayText("Value 1");
        CustomFieldEntity customFieldEntity = getCustomFieldEntity();

        CustomFieldOptionEntity optionEntity = CustomFieldMapper.INSTANCE
                .convertOptionDtoToOptionEntity(option, customFieldEntity);
        assertEquals(optionEntity.getDisplayText(), option.getDisplayText());
        assertEquals(optionEntity.getOptionValue(), option.getOptionValue());
        assertEquals(optionEntity.getCustomField().getId(), customFieldEntity.getId());
    }

    private CustomFieldEntity getCustomFieldEntity() {
        CustomFieldEntity customFieldEntity = new CustomFieldEntity();
        customFieldEntity.setId(UUID.randomUUID());
        customFieldEntity.setName("CUSTOM_FIELD_1");

        CustomFieldTypeEntity customFieldTypeEntity = new CustomFieldTypeEntity();
        customFieldTypeEntity.setId(UUID.fromString("a80a48b5-2995-4c54-9bd5-ebc258fab4ba"));
        customFieldTypeEntity.setType("drop_down_list");
        customFieldEntity.setDisplayText("Custom Field 1");
        customFieldEntity.setType(customFieldTypeEntity);

        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("3e724ddf-4d09-452b-ae98-a8e3a76af19c"));
        customFieldDataTypeEntity.setType("string");

        customFieldEntity.setDataType(customFieldDataTypeEntity);
        CustomFieldOptionEntity option = new CustomFieldOptionEntity();
        option.setId(UUID.randomUUID());
        option.setOptionValue("VALUE_1");

        customFieldEntity.setOptions(List.of(option));

        return customFieldEntity;
    }
}
