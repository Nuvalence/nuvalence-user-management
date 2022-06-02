package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.mapper.UserPreferenceEntityMapper;
import io.nuvalence.user.management.api.service.repository.ApplicationPreferencesRepository;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.UserPreferencesRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserPreferencesServiceTest {

    @Mock
    UserPreferencesRepository userPreferencesRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    ApplicationPreferencesRepository applicationPreferencesRepository;

    @InjectMocks
    UserPreferenceService userPreferenceService;

    @Captor
    private ArgumentCaptor<UserPreferenceEntity> userPreferencesCaptor;

    @Test
    public void getPreferencesByUserId_validUserId() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@website.com");
        user.setId(UUID.randomUUID());
        user.setDisplayName("Generic Admin");

        UserPreferenceEntity preferences = createUserPreference();

        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(Optional.of(preferences));

        UserPreferenceEntity result = userPreferenceService.getPreferencesByUserId(user.getId());

        assertEquals(preferences, result);
    }

    /**
     * Tests if an error is thrown when an invalid user ID is passed to the service.
     */
    @Test
    public void getPreferencesByUserId_invalidUserId() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@website.com");
        user.setId(UUID.fromString("ce90e648-c66c-11ec-871d-2aaa794f39fb"));
        user.setDisplayName("Generic Admin");

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userPreferenceService.getPreferencesByUserId(UUID.fromString("e9721102-c66d-11ec-9abf-2aaa794f39fb"));
        });

        assertTrue(exception.getMessage().contains("Preferences not found for given user!"));
    }

    /**
     * Tests if, given an application with specific overrides, that the returned user preferences
     * is combined with the application specific overrides.
     */
    @Test
    public void getSupportedPreferencesByUserId_validUserId() {
        LanguageEntity backupLanguage = new LanguageEntity();
        backupLanguage.setId(UUID.randomUUID());
        backupLanguage.setLanguageName("English");
        backupLanguage.setLanguageStandardId("en");

        UserEntity user = new UserEntity();
        user.setEmail("admin@website.com");
        user.setId(UUID.randomUUID());
        user.setDisplayName("Generic Admin");

        UserPreferenceEntity preferences = createUserPreference();

        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());
        application.setName("BASIC_APPLICATION");

        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(Optional.of(preferences));
        when(applicationRepository.getApplicationById(application.getId())).thenReturn(Optional.of(application));
        when(applicationPreferencesRepository.findApplicationPreferenceByApplicationId(user.getId(),
                application.getId())).thenReturn(Optional.of(preferences));

        UserPreferenceDTO expected = new UserPreferenceDTO()
                .id(preferences.getId());

        UserPreferenceEntity result = userPreferenceService.getSupportedPreferencesByUserId(
                user.getId(), application.getId()
        );

        assertEquals(expected, UserPreferenceEntityMapper.INSTANCE.userPreferencesEntityToDto(result));
    }

    /**
     * Tests if an error is thrown if no user can be found.
     */
    @Test
    public void getSupportedPreferencesByUserId_invalidUserId() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@website.com");
        user.setId(UUID.fromString("ce90e648-c66c-11ec-871d-2aaa794f39fb"));
        user.setDisplayName("Generic Admin");

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userPreferenceService.getSupportedPreferencesByUserId(
                    UUID.fromString("e9721102-c66d-11ec-9abf-2aaa794f39fb"),
                    null
            );
        });

        assertTrue(exception.getMessage().contains("Preferences not found for given user!"));
    }

    /**
     * Tests if, given an invalid application, an error is thrown. There
     * does not need to be application preferences, however the application
     * must exist.
     */
    @Test
    public void getSupportedPreferencesByUserId_invalidApplicationId() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@website.com");
        user.setId(UUID.fromString("ce90e648-c66c-11ec-871d-2aaa794f39fb"));
        user.setDisplayName("Generic Admin");

        UserPreferenceEntity preferences = createUserPreference();

        UUID randomUuid = UUID.randomUUID();

        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(Optional.of(preferences));
        when(applicationRepository.getApplicationById(randomUuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userPreferenceService.getSupportedPreferencesByUserId(
                    user.getId(),
                    randomUuid // there are no applications added, so any UUID is invalid, i.e. not in the table
            );
        });

        assertTrue(exception.getMessage().contains("Application not found with given ID!"));
    }

    /**
     * Tests if, given a valid application, the *general* preferences are
     * returned. If there are no application preferences for a given user,
     * then all of their user preferences are supported-- thus, no application
     * preferences are necessary.
     */
    @Test
    public void getSupportedPreferencesByUserId_noApplicationPreferences() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@website.com");
        user.setId(UUID.randomUUID());
        user.setDisplayName("Generic Admin");

        UserPreferenceEntity preferences = createUserPreference();

        ApplicationEntity application = new ApplicationEntity();
        application.setName("test-app");
        application.setId(UUID.randomUUID());

        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(Optional.of(preferences));
        when(applicationRepository.getApplicationById(application.getId())).thenReturn(Optional.of(application));

        UserPreferenceEntity result = userPreferenceService.getSupportedPreferencesByUserId(
                user.getId(), application.getId()
        );

        assertEquals(
                preferences,
                result
        );
    }

    /**
     * Tests if the user preferences of a given user can be updated.
     */
    @Test
    public void updateUserPreferences_validUserId() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@website.com");
        user.setId(UUID.randomUUID());
        user.setDisplayName("Generic Admin");

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        LanguageEntity language = new LanguageEntity();
        language.setId(UUID.randomUUID());
        language.setLanguageName("English");
        language.setLanguageStandardId("en");

        UserPreferenceEntity updatedPreferences = new UserPreferenceEntity();

        UserPreferenceDTO updatedPreferencesDto = UserPreferenceEntityMapper
                .INSTANCE
                .userPreferencesEntityToDto(updatedPreferences);

        userPreferenceService.updatePreferencesByUserId(user.getId(), updatedPreferencesDto);

        verify(userPreferencesRepository).save(userPreferencesCaptor.capture());

        UserPreferenceEntity preferenceCaptured = userPreferencesCaptor.getValue();

        //        LanguageEntity resultLanguage = preferenceCaptured.getLanguage();
        //        assertEquals(resultLanguage.getId(), language.getId());
        //        assertEquals(resultLanguage.getLanguageName(), language.getLanguageName());
        //        assertEquals(resultLanguage.getLanguageStandardId(), language.getLanguageStandardId());
    }

    /**
     * Tests if an error is thrown when an invalid User ID is given.
     */
    @Test
    public void updateUserPreferences_invalidUserId() {
        // doesn't matter what the ID is, as long as it doesn't exist
        UUID randomUuid = UUID.randomUUID();

        when(userRepository.findById(randomUuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userPreferenceService.updatePreferencesByUserId(
                    randomUuid, null
            );
        });

        assertEquals(exception.getMessage(), "Cannot find user with given ID!");
    }

    /**
     * Tests if, given a valid application and user, application preferences can be
     * updated.
     */
    @Test
    public void updateApplicationPreferences_validUserValidApplication() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());

        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());

        LanguageEntity backupLanguage = new LanguageEntity();
        backupLanguage.setId(UUID.randomUUID());
        backupLanguage.setLanguageName("English");
        backupLanguage.setLanguageStandardId("en");

        UserPreferenceEntity preferences = createUserPreference();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(applicationRepository.getApplicationById(application.getId())).thenReturn(Optional.of(application));
        when(userPreferencesRepository.findApplicationPreferencesByUserId(user.getId(), application.getId()))
                .thenReturn(Optional.of(preferences));

        UserPreferenceDTO applicationPreferences = new UserPreferenceDTO();

        userPreferenceService.updateApplicationPreferencesById(
                user.getId(), application.getId(), applicationPreferences
        );

        verify(userPreferencesRepository).save(userPreferencesCaptor.capture());

        UserPreferenceEntity preferenceCaptured = userPreferencesCaptor.getValue();
        assertEquals(application.getId(), preferenceCaptured.getApplicationId());
        assertEquals(preferenceCaptured.getUserId(), user.getId());

        //                LanguageEntity resultLanguage = preferenceCaptured.getLanguage();
        //                assertEquals(resultLanguage.getId(), backupLanguage.getId());
        //                assertEquals(resultLanguage.getLanguageName(), backupLanguage.getLanguageName());
        //                assertEquals(resultLanguage.getLanguageStandardId(), backupLanguage.getLanguageStandardId());
    }

    /**
     * Helper method for creating a new user preference.
     * @return User preference.
     */
    private UserPreferenceEntity createUserPreference() {
        LanguageEntity language = new LanguageEntity();
        language.setId(UUID.randomUUID());
        language.setLanguageName("Greek");
        language.setLanguageStandardId("el");

        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setId(UUID.randomUUID());

        return preference;
    }

}
