package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.CustomFieldDataTypeEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldOptionEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldTypeEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserCustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class MapperUtilsTest {

    @Test
    public void mapRoleEntitiesToRoleList_completesMappingAsExpected() {
        RoleEntity roleEntity = new RoleEntity();
        roleEntity.setId(UUID.fromString("af102616-4207-4850-adc4-0bf91058a261"));
        roleEntity.setRoleName("ROLE_TO_TEST_1");
        RoleEntity roleEntity1 = new RoleEntity();
        roleEntity1.setId(UUID.fromString("1b56e8df-a24b-4036-a5d4-7e5b1ca40f9d"));
        roleEntity1.setRoleName("ROLE_TO_TEST_2");

        List<RoleEntity> roleEntities = List.of(roleEntity, roleEntity1);

        List<RoleDTO> mappedRoles = MapperUtils.mapRoleEntitiesToRoleList(roleEntities);
        assertEquals(mappedRoles.size(), roleEntities.size());

        for (int i = 0; i < mappedRoles.size(); i++) {
            assertEquals(mappedRoles.get(i).getId(), roleEntities.get(i).getId());
            assertEquals(mappedRoles.get(i).getRoleName(), roleEntities.get(i).getRoleName());
        }

    }

    /**
     * Tests if, given an application preferences entity with suitable overrides, that
     * it will return a DTO with the user's preferences merged with that of the application.
     */
    @Test
    public void overlapPreferences_overlapping() {
        LanguageEntity language = new LanguageEntity();
        language.setId(UUID.randomUUID());
        language.setLanguageName("Greek");
        language.setLanguageStandardId("el");

        LanguageEntity backupLanguage = new LanguageEntity();
        backupLanguage.setId(UUID.randomUUID());
        backupLanguage.setLanguageName("English");
        backupLanguage.setLanguageStandardId("en");

        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setId(UUID.randomUUID());

        UserPreferenceEntity applicationPreferences = new UserPreferenceEntity();
        applicationPreferences.setId(UUID.randomUUID());

        UserPreferenceDTO expected = new UserPreferenceDTO()
                .id(preference.getId());

        UserPreferenceDTO result = MapperUtils.overlapPreferences(preference, applicationPreferences);

        assertEquals(expected, result);
    }

    /**
     * Tests if, given an application preferences entity with __no__ overlapping properties, if
     * the resultant User Preferences DTO will default to the original properties.
     */
    @Test
    public void overlapPreferences_nonOverlapping() {
        LanguageEntity language = new LanguageEntity();
        language.setId(UUID.randomUUID());
        language.setLanguageName("Greek");
        language.setLanguageStandardId("el");

        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setId(UUID.randomUUID());

        UserPreferenceEntity applicationPreferences = new UserPreferenceEntity();
        applicationPreferences.setId(UUID.randomUUID());

        // Ignore all null properties, default to those in the user preferences
        UserPreferenceDTO expected = new UserPreferenceDTO()
                .id(preference.getId());

        UserPreferenceDTO result = MapperUtils.overlapPreferences(preference, applicationPreferences);

        assertEquals(expected, result);
    }

    @Test
    public void shouldMapUserCustomFieldEntityToDto() {
        CustomFieldOptionEntity option = new CustomFieldOptionEntity();
        option.setId(UUID.randomUUID());
        option.setOptionValue("VALUE_1");
        option.setDisplayText("Value 1");

        CustomFieldEntity customFieldEntity = new CustomFieldEntity();
        customFieldEntity.setId(UUID.randomUUID());
        customFieldEntity.setName("CUSTOM_FIELD_1");
        CustomFieldTypeEntity customFieldTypeEntity = new CustomFieldTypeEntity();
        customFieldTypeEntity.setId(UUID.fromString("a80a48b5-2995-4c54-9bd5-ebc258fab4ba"));
        customFieldTypeEntity.setType("drop_down_list");
        customFieldEntity.setType(customFieldTypeEntity);
        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("3e724ddf-4d09-452b-ae98-a8e3a76af19c"));
        customFieldDataTypeEntity.setType("string");
        customFieldEntity.setDataType(customFieldDataTypeEntity);
        customFieldEntity.setDisplayText("Custom Field 1");
        customFieldEntity.setOptions(List.of(option));

        UserCustomFieldEntity userCustomFieldEntity = new UserCustomFieldEntity();
        userCustomFieldEntity.setId(UUID.randomUUID());
        userCustomFieldEntity.setCustomField(customFieldEntity);
        userCustomFieldEntity.setCustomFieldValueString("TEST1");

        UserCustomFieldDTO userCustomFieldModel = MapperUtils.mapUserCustomFieldEntityToDto(userCustomFieldEntity);
        assertEquals(userCustomFieldModel.getId(), userCustomFieldEntity.getId());
        assertEquals(userCustomFieldModel.getCustomFieldId(), userCustomFieldEntity.getCustomField().getId());
        assertEquals(userCustomFieldModel.getType().getValue(),
                userCustomFieldEntity.getCustomField().getType().getType());
        assertEquals(userCustomFieldModel.getDataType().getValue(),
                userCustomFieldEntity.getCustomField().getDataType().getType());
        assertEquals(userCustomFieldModel.getName(), userCustomFieldEntity.getCustomField().getName());
        assertEquals(userCustomFieldModel.getDisplayText(), userCustomFieldEntity.getCustomField().getDisplayText());
        assertEquals(userCustomFieldModel.getValue(), "TEST1");
        assertEquals(userCustomFieldModel.getOptions().get(0).getOptionValue(),
                userCustomFieldEntity.getCustomField().getOptions().get(0).getOptionValue());
    }
}
