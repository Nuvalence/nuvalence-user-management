package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationPreferenceDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.mapper.UserPreferenceEntityMapper;
import io.nuvalence.user.management.api.service.repository.ApplicationPreferencesRepository;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.UserPreferencesRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;
import javax.transaction.Transactional;

/**
 * Service for User Preferences.
 */
@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserPreferenceService {

    private final UserRepository userRepository;
    private final UserPreferencesRepository userPreferencesRepository;
    private final ApplicationPreferencesRepository applicationPreferencesRepository;
    private final ApplicationRepository applicationRepository;


    /**
     * Returns a user's supported preferences based on their ID and application ID. A user's general
     * preferences may not be supported by a given application; getting the supported preferences will
     * override the conflicting preferences with application-specific preferences.
     *
     * @param userId User's ID.
     * @param appId  Application ID.
     * @return User Preferences.
     */
    public UserPreferenceEntity getSupportedPreferencesByUserId(UUID userId, UUID appId) {
        UserPreferenceEntity preferences = this.getPreferencesByUserId(userId);

        Optional<ApplicationEntity> application = applicationRepository.getApplicationById(appId);

        if (application.isEmpty()) {
            throw new ResourceNotFoundException("Application not found with given ID!");
        }

        Optional<ApplicationPreferenceEntity> applicationPreferences = applicationPreferencesRepository
                .findApplicationPreferenceByApplicationId(userId, appId);

        // if there are no application-specific preferences, just send over the general preferences.
        if (applicationPreferences.isEmpty()) {
            return preferences;
        }

        // combine the two together; results in one user preferences entity where each field is supported
        return MapperUtils.overlapPreferenceEntities(preferences, applicationPreferences.get());
    }

    /**
     * Returns a user's general preferences based on their ID. General preferences are those that
     * may or may not be supported by the requesting application.
     *
     * @param userId user's id.
     * @return User Preferences.
     */
    public UserPreferenceEntity getPreferencesByUserId(UUID userId) {
        Optional<UserPreferenceEntity> preferences = userPreferencesRepository.findPreferencesByUserId(userId);
        log.info("called repo");

        if (preferences.isEmpty()) {
            throw new ResourceNotFoundException("Preferences not found for given user!");
        }

        return preferences.get();
    }

    /**
     * For a given user, override their user preferences.
     * @param userId User ID.
     * @param updatedPreferences Updated preferences.
     */
    public void updatePreferencesByUserId(UUID userId, UserPreferenceDTO updatedPreferences) {
        Optional<UserEntity> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new ResourceNotFoundException("Cannot find user with given ID!");
        }

        Optional<UserPreferenceEntity> initialPreferences = userPreferencesRepository
                .findPreferencesByUserId(user.get().getId());

        UserPreferenceEntity preferences = UserPreferenceEntityMapper
                .INSTANCE
                .userPreferencesDtoToEntity(updatedPreferences);

        // if there are already preferences, keep the ID. Otherwise, generate a new one.
        preferences.setId(initialPreferences.isEmpty() ? UUID.randomUUID() : initialPreferences.get().getId());

        // associate the updated preferences with the user
        preferences.setUserId(user.get().getId());

        userPreferencesRepository.save(preferences);
    }

    /**
     * Replaces the user preferences for a specific application.
     * @param userId User ID.
     * @param appId Application ID.
     * @param updatedPreferences Updated application preferences.
     */
    public void updateApplicationPreferencesById(UUID userId, UUID appId,
                                                                 ApplicationPreferenceDTO updatedPreferences) {
        Optional<UserEntity> user = userRepository.findById(userId);
        Optional<ApplicationEntity> application = applicationRepository.getApplicationById(appId);

        if (user.isEmpty() || application.isEmpty()) {
            throw new ResourceNotFoundException("Cannot find user or application with given ID!");
        }

        ApplicationPreferenceEntity preferences = UserPreferenceEntityMapper
                .INSTANCE.applicationPreferencesDtoToEntity(updatedPreferences);

        Optional<UserPreferenceEntity> userPreference = userPreferencesRepository.findPreferencesByUserId(userId);

        if (userPreference.isEmpty()) {
            throw new ResourceNotFoundException(
                    "Cannot assign application preferences for user with no default user preferences."
            );
        }

        preferences.setUser(user.get());
        preferences.setUserPreferenceId(userPreference.get());
        preferences.setApplication(application.get());

        applicationPreferencesRepository.save(preferences);
    }

}
