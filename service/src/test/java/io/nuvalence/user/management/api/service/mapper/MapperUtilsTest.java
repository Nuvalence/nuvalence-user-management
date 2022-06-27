package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldDataTypeEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldOptionEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldTypeEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserCustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceOptionEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceTypeEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());

        List<UserPreferenceEntity> userPreferences = createUserPreferences(user);
        List<UserPreferenceEntity> userApplicationPreferences = createUserApplicationPreferences(user, application);

        UserPreferenceDTO expected = new UserPreferenceDTO();
        expected.userId(user.getId());
        expected.applicationId(application.getId());
        expected.putAll(userApplicationPreferences.stream()
                .collect(Collectors.toMap(a -> a.getType().getName(), a -> a.getOption().getValue())));

        UserPreferenceDTO result = MapperUtils.overlapPreferences(userPreferences, userApplicationPreferences,
                user.getId(), application.getId());
        assertEquals(expected, result);
    }

    /**
     * Tests if, given an application preferences entity with __no__ overlapping properties, if
     * the resultant User Preferences DTO will default to the original properties.
     */
    @Test
    public void overlapPreferences_nonOverlapping() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());

        List<UserPreferenceEntity> userPreferences = createUserPreferences(user);
        List<UserPreferenceEntity> userApplicationPreferences = Collections.emptyList();

        UserPreferenceDTO expected = new UserPreferenceDTO();
        expected.userId(user.getId());
        expected.applicationId(application.getId());
        expected.putAll(userPreferences.stream()
                .collect(Collectors.toMap(a -> a.getType().getName(), a -> a.getOption().getValue())));

        UserPreferenceDTO result = MapperUtils.overlapPreferences(userPreferences, userApplicationPreferences,
                user.getId(), application.getId());
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

    /**
     * Helper method for creating a list of user preferences.
     * @param user User.
     * @return User preferences.
     */
    private List<UserPreferenceEntity> createUserPreferences(UserEntity user) {
        UserPreferenceTypeEntity languagePreferenceType = new UserPreferenceTypeEntity();
        languagePreferenceType.setId(UUID.fromString("e8dc1b46-65a1-4e1f-9516-009560a50a2f"));
        languagePreferenceType.setName("language");

        UserPreferenceTypeEntity communicationPreferenceType = new UserPreferenceTypeEntity();
        communicationPreferenceType.setId(UUID.fromString("3505d910-a479-423b-b3f8-a3d16798a651"));
        communicationPreferenceType.setName("communication");

        UserPreferenceOptionEntity emailOption = new UserPreferenceOptionEntity();
        emailOption.setId(UUID.fromString("0e3929a9-fabc-4d73-8cfe-14c89c51b531"));
        emailOption.setUserPreferenceType(communicationPreferenceType);
        emailOption.setValue("email");

        UserPreferenceOptionEntity phoneOption = new UserPreferenceOptionEntity();
        phoneOption.setId(UUID.fromString("3505d910-a479-423b-b3f8-a3d16798a651"));
        phoneOption.setUserPreferenceType(communicationPreferenceType);
        phoneOption.setValue("phone");

        UserPreferenceOptionEntity englishOption = new UserPreferenceOptionEntity();
        englishOption.setId(UUID.fromString("c95afee7-2bc4-4c12-acdd-344b5432520e"));
        englishOption.setUserPreferenceType(languagePreferenceType);
        englishOption.setValue("en");

        UserPreferenceOptionEntity spanishOption = new UserPreferenceOptionEntity();
        spanishOption.setId(UUID.fromString("b4449fdb-2f08-4c56-9c43-f0da407787d8"));
        spanishOption.setUserPreferenceType(languagePreferenceType);
        spanishOption.setValue("es");

        UserPreferenceEntity languagePreference = new UserPreferenceEntity();
        languagePreference.setId(UUID.randomUUID());
        languagePreference.setUser(user);
        languagePreference.setType(languagePreferenceType);
        languagePreference.setOption(englishOption);

        UserPreferenceEntity communicationPreference = new UserPreferenceEntity();
        communicationPreference.setId(UUID.randomUUID());
        communicationPreference.setUser(user);
        communicationPreference.setType(communicationPreferenceType);
        communicationPreference.setOption(phoneOption);

        communicationPreferenceType.setUserPreferenceOptionEntities(List.of(emailOption, phoneOption));
        languagePreferenceType.setUserPreferenceOptionEntities(List.of(englishOption, spanishOption));

        return List.of(languagePreference, communicationPreference);
    }

    /**
     * Helper method for creating a list of user application preferences.
     * @param user User.
     * @param application Application.
     * @return User preferences.
     */
    private List<UserPreferenceEntity> createUserApplicationPreferences(UserEntity user,
                                                                        ApplicationEntity application) {
        UserPreferenceTypeEntity languagePreferenceType = new UserPreferenceTypeEntity();
        languagePreferenceType.setId(UUID.fromString("e8dc1b46-65a1-4e1f-9516-009560a50a2f"));
        languagePreferenceType.setName("language");

        UserPreferenceTypeEntity communicationPreferenceType = new UserPreferenceTypeEntity();
        communicationPreferenceType.setId(UUID.fromString("3505d910-a479-423b-b3f8-a3d16798a651"));
        communicationPreferenceType.setName("communication");

        UserPreferenceOptionEntity emailOption = new UserPreferenceOptionEntity();
        emailOption.setId(UUID.fromString("0e3929a9-fabc-4d73-8cfe-14c89c51b531"));
        emailOption.setUserPreferenceType(communicationPreferenceType);
        emailOption.setValue("email");

        UserPreferenceOptionEntity phoneOption = new UserPreferenceOptionEntity();
        phoneOption.setId(UUID.fromString("3505d910-a479-423b-b3f8-a3d16798a651"));
        phoneOption.setUserPreferenceType(communicationPreferenceType);
        phoneOption.setValue("phone");

        UserPreferenceOptionEntity englishOption = new UserPreferenceOptionEntity();
        englishOption.setId(UUID.fromString("c95afee7-2bc4-4c12-acdd-344b5432520e"));
        englishOption.setUserPreferenceType(languagePreferenceType);
        englishOption.setValue("en");

        UserPreferenceOptionEntity spanishOption = new UserPreferenceOptionEntity();
        spanishOption.setId(UUID.fromString("b4449fdb-2f08-4c56-9c43-f0da407787d8"));
        spanishOption.setUserPreferenceType(languagePreferenceType);
        spanishOption.setValue("es");

        UserPreferenceEntity languagePreference = new UserPreferenceEntity();
        languagePreference.setId(UUID.randomUUID());
        languagePreference.setUser(user);
        languagePreference.setType(languagePreferenceType);
        languagePreference.setOption(spanishOption);

        UserPreferenceEntity communicationPreference = new UserPreferenceEntity();
        communicationPreference.setId(UUID.randomUUID());
        communicationPreference.setUser(user);
        communicationPreference.setType(communicationPreferenceType);
        communicationPreference.setOption(emailOption);

        languagePreference.setApplication(application);
        communicationPreference.setApplication(application);

        communicationPreferenceType.setUserPreferenceOptionEntities(List.of(emailOption, phoneOption));
        languagePreferenceType.setUserPreferenceOptionEntities(List.of(englishOption, spanishOption));

        return List.of(languagePreference, communicationPreference);
    }
}
