package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceOptionEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceTypeEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.UserPreferenceTypeRepository;
import io.nuvalence.user.management.api.service.repository.UserPreferencesRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class UserPreferenceServiceTest {

    @Mock
    UserPreferencesRepository userPreferencesRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    ApplicationRepository applicationRepository;

    @Mock
    UserPreferenceTypeRepository userPreferenceTypeRepository;

    @InjectMocks
    UserPreferenceService userPreferenceService;

    @Captor
    private ArgumentCaptor<Iterable<UserPreferenceEntity>> userPreferencesCaptor;

    @Test
    public void getUserPreferences_validUserIdNoAppId() {
        UserEntity user = createUserEntity();
        List<UserPreferenceEntity> userPreferences = createUserPreferences(user);
        UserPreferenceDTO preferences = createUserPreferenceDto(user.getId());
        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(userPreferences);

        UserPreferenceDTO result = userPreferenceService.getUserPreferences(user.getId(), null);

        verify(userPreferencesRepository, never()).findUserApplicationPreferences(any(), any());
        assertEquals(preferences, result);
    }

    /**
     * Tests if an error is thrown when an invalid user ID is passed to the service.
     */
    @Test
    public void getUserPreferences_invalidUserIdNoAppId() {
        UserEntity user = createUserEntity();
        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(Collections.emptyList());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                userPreferenceService.getUserPreferences(user.getId(), null));

        assertTrue(exception.getMessage().contains("Preferences not found for given user!"));
        verify(userPreferencesRepository, never()).findUserApplicationPreferences(any(), any());
    }

    /**
     * Tests if, given an application with specific overrides, that the returned user preferences
     * is combined with the application specific overrides.
     */
    @Test
    public void getUserApplicationPreferences_validUserIdValidAppId() {
        UserEntity user = createUserEntity();
        ApplicationEntity application = createApplicationEntity();

        List<UserPreferenceEntity> userPreferences = createUserPreferences(user);
        List<UserPreferenceEntity> applicationPreferences = createUserApplicationPreferences(user, application);

        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(userPreferences);
        when(userPreferencesRepository.findUserApplicationPreferences(user.getId(),
                application.getId())).thenReturn(applicationPreferences);

        UserPreferenceDTO expected = createUserApplicationPreferenceDto(user.getId(), application.getId());
        assertEquals(expected, userPreferenceService.getUserPreferences(user.getId(), application.getId()));
    }

    /**
     * Tests if, given an application with no overrides, that the returned user preferences
     * are returned instead.
     */
    @Test
    public void getUserApplicationPreferences_hasUserPrefsNoAppPrefs() {
        UserEntity user = createUserEntity();
        ApplicationEntity application = createApplicationEntity();

        List<UserPreferenceEntity> userPreferences = createUserPreferences(user);

        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(userPreferences);
        when(userPreferencesRepository.findUserApplicationPreferences(user.getId(),
                application.getId())).thenReturn(Collections.emptyList());

        UserPreferenceDTO expected = createUserPreferenceDto(user.getId());
        expected.setApplicationId(application.getId());
        assertEquals(expected, userPreferenceService.getUserPreferences(user.getId(), application.getId()));
    }

    /**
     * Tests if an error is thrown if no user can be found.
     */
    @Test
    public void getUserApplicationPreferences_noApplicationPreferences() {
        UserEntity user = createUserEntity();
        ApplicationEntity application = createApplicationEntity();
        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(userPreferencesRepository.findUserApplicationPreferences(user.getId(), application.getId()))
                .thenReturn(Collections.emptyList());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                userPreferenceService.getUserPreferences(user.getId(), application.getId())
        );

        assertTrue(exception.getMessage().contains("Preferences not found for given user!"));
    }

    /**
     * Tests if the user preferences of a given user can be updated.
     */
    @Test
    public void updateUserPreferences_validUserId() {
        UserEntity user = createUserEntity();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        UserPreferenceDTO updatedPreferencesDto = createUserPreferenceDto(user.getId());
        List<UserPreferenceEntity> preferences = createUserPreferences(user);
        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(preferences);
        when(userPreferenceTypeRepository.findAllByNames(any())).thenReturn(
                preferences.stream().map(UserPreferenceEntity::getType).collect(Collectors.toList())
        );

        // setting preferences to something other than what's already set
        updatedPreferencesDto.put("communication", "email");
        updatedPreferencesDto.put("language", "es");

        userPreferenceService.updateUserPreferences(user.getId(), updatedPreferencesDto);

        verify(userPreferencesRepository).saveAll(userPreferencesCaptor.capture());
        Iterable<UserPreferenceEntity> preferenceCaptured = userPreferencesCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(preferenceCaptured), updatedPreferencesDto.entrySet().size());
    }

    /**
     * Tests if the user preferences of a given user can be updated, with no pre-existing preferences.
     */
    @Test
    public void updateUserPreferences_validUserIdNonExistingPreferences() {
        UserEntity user = createUserEntity();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        UserPreferenceDTO updatedPreferencesDto = createUserPreferenceDto(user.getId());
        List<UserPreferenceEntity> preferences = createUserPreferences(user);
        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(Collections.emptyList());
        when(userPreferenceTypeRepository.findAllByNames(any())).thenReturn(
                preferences.stream().map(UserPreferenceEntity::getType).collect(Collectors.toList())
        );

        // setting preferences to something other than what's already set
        updatedPreferencesDto.put("communication", "email");
        updatedPreferencesDto.put("language", "es");

        userPreferenceService.updateUserPreferences(user.getId(), updatedPreferencesDto);

        verify(userPreferencesRepository).saveAll(userPreferencesCaptor.capture());
        Iterable<UserPreferenceEntity> preferenceCaptured = userPreferencesCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(preferenceCaptured), updatedPreferencesDto.entrySet().size());
    }

    /**
     * Tests if an error is thrown when an invalid preference value is given.
     */
    @Test
    public void updateUserPreferences_validUserIdInvalidOption() {
        UserEntity user = createUserEntity();
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        UserPreferenceDTO updatedPreferencesDto = createUserPreferenceDto(user.getId());
        List<UserPreferenceEntity> preferences = createUserPreferences(user);
        when(userPreferencesRepository.findPreferencesByUserId(user.getId())).thenReturn(preferences);
        when(userPreferenceTypeRepository.findAllByNames(any())).thenReturn(
                preferences.stream().map(UserPreferenceEntity::getType).collect(Collectors.toList())
        );

        // setting preferences to something other than what's already set
        updatedPreferencesDto.put("communication", "email");
        updatedPreferencesDto.put("language", "ABC123"); // "ABC123" is a random "language" that shouldn't be valid

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                userPreferenceService.updateUserPreferences(user.getId(), updatedPreferencesDto)
        );

        assertTrue(exception.getMessage().equalsIgnoreCase(
                "No valid option value found for type language and value ABC123.")
        );
    }

    /**
     * Tests if an error is thrown when an invalid User ID is given.
     */
    @Test
    public void updateUserPreferences_invalidUserId() {
        UserEntity user = createUserEntity();
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                userPreferenceService.updateUserPreferences(user.getId(),
                        createUserPreferenceDto(user.getId())));

        assertEquals(exception.getMessage(), "Cannot find user with given ID!");
    }

    /**
     * Tests if, given a valid application and user, application preferences can be
     * updated.
     */
    @Test
    public void updateUserApplicationPreferences_validUserIdValidAppId() {
        UserEntity user = createUserEntity();
        ApplicationEntity application = createApplicationEntity();
        List<UserPreferenceEntity> preferences = createUserApplicationPreferences(user, application);
        when(userPreferencesRepository.findUserApplicationPreferences(user.getId(), application.getId()))
                .thenReturn(preferences);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        UserPreferenceDTO applicationPreferences = createUserApplicationPreferenceDto(user.getId(),
                application.getId());

        // setting preferences to something other than what's already set
        applicationPreferences.put("communication", "phone");
        applicationPreferences.put("language", "en");

        when(userPreferenceTypeRepository.findAllByNames(any())).thenReturn(
                preferences.stream().map(UserPreferenceEntity::getType).collect(Collectors.toList())
        );

        userPreferenceService.updateUserApplicationPreferences(
                user.getId(), application.getId(), applicationPreferences
        );

        verify(userPreferencesRepository).saveAll(userPreferencesCaptor.capture());
        Iterable<UserPreferenceEntity> preferenceCaptured = userPreferencesCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(preferenceCaptured), preferences.size());
    }

    /**
     * Tests if an error is thrown if an invalid User ID is given.
     */
    @Test
    public void updateUserApplicationPreferences_invalidUserId() {
        UserEntity user = createUserEntity();
        ApplicationEntity application = createApplicationEntity();
        UserPreferenceDTO applicationPreferences = createUserApplicationPreferenceDto(user.getId(),
                application.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                userPreferenceService.updateUserApplicationPreferences(user.getId(), application.getId(),
                        applicationPreferences));

        assertTrue(exception.getMessage().contains("Cannot find user with given ID!"));
    }

    /**
     * Tests if an error is throw if an invalid Application ID is given.
     */
    @Test
    public void updateUserApplicationPreferences_invalidAppId() {
        UserEntity user = createUserEntity();
        ApplicationEntity application = createApplicationEntity();
        UserPreferenceDTO applicationPreferences = createUserApplicationPreferenceDto(user.getId(),
                application.getId());
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                userPreferenceService.updateUserApplicationPreferences(user.getId(), application.getId(),
                        applicationPreferences));

        assertTrue(exception.getMessage().contains("Cannot find application with given ID!"));
    }

    /**
     * Tests if an error is thrown if no preferences are given.
     */
    @Test
    public void updateUserApplicationPreferences_validUserIdValidAppIdNoPreferences() {
        UserEntity user = createUserEntity();
        ApplicationEntity application = createApplicationEntity();

        List<UserPreferenceEntity> preferences = createUserApplicationPreferences(user, application);

        when(userPreferencesRepository.findUserApplicationPreferences(user.getId(), application.getId()))
                .thenReturn(preferences);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));

        UserPreferenceDTO applicationPreferences = new UserPreferenceDTO();
        applicationPreferences.setUserId(user.getId());
        applicationPreferences.setApplicationId(application.getId());

        Exception exception = assertThrows(BusinessLogicException.class, () ->
                userPreferenceService.updateUserApplicationPreferences(user.getId(), application.getId(),
                        applicationPreferences));

        assertTrue(exception.getMessage().contains("You must pass in at least one preference to update."));
    }

    @Test
    public void updateUserApplicationPreferences_validUserIdValidAppIdInvalidPreference() {
        UserEntity user = createUserEntity();
        ApplicationEntity application = createApplicationEntity();
        List<UserPreferenceEntity> preferences = createUserApplicationPreferences(user, application);
        when(userPreferencesRepository.findUserApplicationPreferences(user.getId(), application.getId()))
                .thenReturn(preferences);
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
        when(applicationRepository.findById(application.getId())).thenReturn(Optional.of(application));
        UserPreferenceDTO applicationPreferences = createUserApplicationPreferenceDto(user.getId(),
                application.getId());

        // setting a preference that will be invalid
        applicationPreferences.put("invalid_preference", "value");

        when(userPreferenceTypeRepository.findAllByNames(any())).thenReturn(
                preferences.stream().map(UserPreferenceEntity::getType).collect(Collectors.toList())
        );

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                userPreferenceService.updateUserApplicationPreferences(user.getId(), application.getId(),
                        applicationPreferences));

        assertTrue(exception.getMessage().contains(
                String.format("Preference with name '%s' is not valid.", "invalid_preference")
        ));
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

    /**
     * Helper method for creating a new user preference dto.
     * @param userId User id.
     * @return User preference.
     */
    private UserPreferenceDTO createUserPreferenceDto(UUID userId) {
        UserPreferenceDTO preferences = new UserPreferenceDTO();
        preferences.setUserId(userId);
        preferences.put("language", "en");
        preferences.put("communication", "phone");
        return preferences;
    }

    /**
     * Helper method for creating a new user application preference dto.
     * @param userId User id.
     * @param applicationId Application
     * @return User preference.
     */
    private UserPreferenceDTO createUserApplicationPreferenceDto(UUID userId, UUID applicationId) {
        UserPreferenceDTO preferences = new UserPreferenceDTO();
        preferences.setUserId(userId);
        preferences.setApplicationId(applicationId);
        preferences.put("language", "es");
        preferences.put("communication", "email");
        return preferences;
    }

    private ApplicationEntity createApplicationEntity() {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());
        application.setName("BASIC_APPLICATION");
        return application;
    }

    private UserEntity createUserEntity() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setEmail("admin@website.com");
        user.setDisplayName("Generic Admin");
        return user;
    }
}
