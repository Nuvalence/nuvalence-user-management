package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationPreferenceDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.mapper.LanguageEntityMapper;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserPreferencesServiceTest {

    @Mock
    ApplicationPreferencesRepository applicationPreferencesRepository;

    @Mock
    UserPreferencesRepository userPreferencesRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @InjectMocks
    UserPreferenceService userPreferenceService;

    @Captor
    private ArgumentCaptor<UserPreferenceEntity> userPreferencesCaptor;

    @Captor
    private ArgumentCaptor<ApplicationPreferenceEntity> applicationPreferencesCaptor;

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

        ApplicationPreferenceEntity applicationPreferences = new ApplicationPreferenceEntity();
        applicationPreferences.setId(UUID.randomUUID());
        applicationPreferences.setApplication(application);
        applicationPreferences.setUser(user);
        applicationPreferences.setLanguage(backupLanguage);

        when(applicationPreferencesRepository.findApplicationPreferenceByApplicationId(
                user.getId(), application.getId()
        )).thenReturn(Optional.of(applicationPreferences));

        UserPreferenceDTO expected = new UserPreferenceDTO()
                .id(preferences.getId())
                .communicationPreference("sms")
                .language(LanguageEntityMapper.INSTANCE.languageEntityToLanguageDto(backupLanguage));

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
        updatedPreferences.setCommunicationPreference("email");
        updatedPreferences.setLanguage(language);

        UserPreferenceDTO updatedPreferencesDto = UserPreferenceEntityMapper
                .INSTANCE
                .userPreferencesEntityToDto(updatedPreferences);

        userPreferenceService.updatePreferencesByUserId(user.getId(), updatedPreferencesDto);

        verify(userPreferencesRepository).save(userPreferencesCaptor.capture());

        UserPreferenceEntity preferenceCaptured = userPreferencesCaptor.getValue();
        assertEquals(preferenceCaptured.getCommunicationPreference(), updatedPreferences.getCommunicationPreference());

        LanguageEntity resultLanguage = preferenceCaptured.getLanguage();
        assertEquals(resultLanguage.getId(), language.getId());
        assertEquals(resultLanguage.getLanguageName(), language.getLanguageName());
        assertEquals(resultLanguage.getLanguageStandardId(), language.getLanguageStandardId());
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
        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(Optional.of(preferences));

        ApplicationPreferenceDTO applicationPreferences = new ApplicationPreferenceDTO()
                .language(LanguageEntityMapper.INSTANCE.languageEntityToLanguageDto(backupLanguage));

        userPreferenceService.updateApplicationPreferencesById(
                user.getId(), application.getId(), applicationPreferences
        );

        verify(applicationPreferencesRepository).save(applicationPreferencesCaptor.capture());

        ApplicationPreferenceEntity preferenceCaptured = applicationPreferencesCaptor.getValue();
        assertEquals(application, preferenceCaptured.getApplication());
        assertEquals(preferenceCaptured.getUser(), user);

        LanguageEntity resultLanguage = preferenceCaptured.getLanguage();
        assertEquals(resultLanguage.getId(), backupLanguage.getId());
        assertEquals(resultLanguage.getLanguageName(), backupLanguage.getLanguageName());
        assertEquals(resultLanguage.getLanguageStandardId(), backupLanguage.getLanguageStandardId());
    }

    /**
     * Tests if, given an invalid user, and error is thrown.
     */
    @Test
    public void updateApplicationPreferences_invalidUserValidApplication() {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());
        application.setName("my-awesome-application");

        UUID randomUuid = UUID.randomUUID();

        when(userRepository.findById(randomUuid)).thenReturn(Optional.empty());
        when(applicationRepository.getApplicationById(application.getId())).thenReturn(Optional.of(application));

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userPreferenceService.updateApplicationPreferencesById(
                    randomUuid,
                    application.getId(), null);
        });

        assertTrue(exception.getMessage().contains("Cannot find user or application with given ID!"));
    }

    /**
     * Tests if, given an invalid application, and error is thrown.
     */
    @Test
    public void updateApplicationPreferences_validUserInvalidApplication() {
        UserEntity user = new UserEntity();
        user.setEmail("admin@website.com");
        user.setId(UUID.fromString("ce90e648-c66c-11ec-871d-2aaa794f39fb"));
        user.setDisplayName("Generic Admin");

        UUID randomUuid = UUID.randomUUID();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(applicationRepository.getApplicationById(randomUuid)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userPreferenceService.updateApplicationPreferencesById(
                    user.getId(),
                    randomUuid,
                    null);
        });

        assertTrue(exception.getMessage().contains("Cannot find user or application with given ID!"));
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
        preference.setLanguage(language);
        preference.setCommunicationPreference("sms");

        return preference;
    }

}
