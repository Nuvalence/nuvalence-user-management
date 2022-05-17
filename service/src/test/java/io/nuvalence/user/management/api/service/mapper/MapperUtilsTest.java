package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.ApplicationPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
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
        preference.setCommunicationPreference("sms");
        preference.setLanguage(language);
        preference.setId(UUID.randomUUID());

        ApplicationPreferenceEntity applicationPreferences = new ApplicationPreferenceEntity();
        applicationPreferences.setId(UUID.randomUUID());
        applicationPreferences.setLanguage(backupLanguage);

        UserPreferenceDTO expected = new UserPreferenceDTO()
                .id(preference.getId())
                .communicationPreference("sms")
                .language(LanguageEntityMapper.INSTANCE.languageEntityToLanguageDto(backupLanguage));

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
        preference.setCommunicationPreference("sms");
        preference.setLanguage(language);
        preference.setId(UUID.randomUUID());

        ApplicationPreferenceEntity applicationPreferences = new ApplicationPreferenceEntity();
        applicationPreferences.setId(UUID.randomUUID());
        applicationPreferences.setLanguage(null);

        // Ignore all null properties, default to those in the user preferences
        UserPreferenceDTO expected = new UserPreferenceDTO()
                .id(preference.getId())
                .communicationPreference("sms")
                .language(LanguageEntityMapper.INSTANCE.languageEntityToLanguageDto(language));

        UserPreferenceDTO result = MapperUtils.overlapPreferences(preference, applicationPreferences);

        assertEquals(expected, result);
    }
}
