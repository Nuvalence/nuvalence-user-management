package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceOptionEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceTypeEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import io.nuvalence.user.management.api.service.repository.UserPreferenceTypeRepository;
import io.nuvalence.user.management.api.service.repository.UserPreferencesRepository;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private final UserPreferenceTypeRepository userPreferenceTypeRepository;
    private final ApplicationRepository applicationRepository;

    /**
     * Returns user preferences, optionally by application.
     * 
     * @param userId user's id.
     * @param appId app id.
     * @return User Preferences.
     */
    public UserPreferenceDTO getUserPreferences(UUID userId, UUID appId) {
        List<UserPreferenceEntity> userPreferences =
                userPreferencesRepository.findPreferencesByUserId(userId);
        List<UserPreferenceEntity> applicationPreferences = new ArrayList<>();

        if (userId != null && appId != null) {
            applicationPreferences = userPreferencesRepository.findUserApplicationPreferences(userId, appId);
        }

        if (userPreferences.isEmpty() && applicationPreferences.isEmpty()) {
            throw new ResourceNotFoundException("Preferences not found for given user!");
        }

        return MapperUtils.overlapPreferences(userPreferences, applicationPreferences, userId, appId);
    }

    /**
     * For a given user, override their user preferences.
     * @param userId User ID.
     * @param updatedPreferences Updated preferences.
     */
    public void updateUserPreferences(UUID userId, Map<String, String> updatedPreferences) {
        Optional<UserEntity> user = userRepository.findById(userId);

        if (user.isEmpty()) {
            throw new ResourceNotFoundException("Cannot find user with given ID!");
        }

        List<UserPreferenceEntity> initialPreferences = userPreferencesRepository
                .findPreferencesByUserId(user.get().getId());

        persistPreferences(updatedPreferences, initialPreferences, user.get(), null);
    }

    /**
     * For a given user and application, override their user preferences.
     * @param userId User ID.
     * @param appId Application ID.
     * @param updatedPreferences Updated preferences.
     */
    public void updateUserApplicationPreferences(UUID userId, UUID appId, Map<String, String> updatedPreferences) {
        Optional<UserEntity> user = userRepository.findById(userId);
        if (user.isEmpty()) {
            throw new ResourceNotFoundException("Cannot find user with given ID!");
        }

        Optional<ApplicationEntity> application = applicationRepository.findById(appId);
        if (application.isEmpty()) {
            throw new ResourceNotFoundException("Cannot find application with given ID!");
        }

        List<UserPreferenceEntity> initialPreferences = userPreferencesRepository
                .findUserApplicationPreferences(user.get().getId(), application.get().getId());

        persistPreferences(updatedPreferences, initialPreferences, user.get(), application.get());
    }

    private void persistPreferences(Map<String, String> updatedPreferences,
                                    List<UserPreferenceEntity> initialPreferences,
                                    UserEntity user, ApplicationEntity application) {
        List<UserPreferenceEntity> persistedPreferences = new ArrayList<>();

        if (updatedPreferences == null || updatedPreferences.isEmpty()) {
            throw new BusinessLogicException("You must pass in at least one preference to update.");
        }

        // get all preference types by their names
        Map<String, UserPreferenceTypeEntity> foundPreferenceTypes = userPreferenceTypeRepository
                .findAllByNames(new ArrayList<>(updatedPreferences.keySet()))
                .stream().collect(Collectors.toMap(t -> t.getName().toLowerCase(), t -> t));

        for (Map.Entry<String, String> entry : updatedPreferences.entrySet()) {
            if (!foundPreferenceTypes.containsKey(entry.getKey().toLowerCase())) {
                throw new ResourceNotFoundException(
                        String.format("Preference with name '%s' is not valid.", entry.getKey())
                );
            }

            UserPreferenceTypeEntity preferenceType = foundPreferenceTypes.get(entry.getKey().toLowerCase());

            Optional<UserPreferenceEntity> existingUserPreference = initialPreferences.stream().filter(p ->
                    p.getType().getId().compareTo(preferenceType.getId()) == 0).findFirst();

            Optional<UserPreferenceOptionEntity> foundOption = Optional.empty();
            List<UserPreferenceOptionEntity> options = preferenceType.getUserPreferenceOptionEntities();
            if (options != null && !options.isEmpty()) {
                foundOption = options.stream()
                        .filter(o -> o.getValue().equalsIgnoreCase(entry.getValue())).findFirst();
            }

            // the value passed in was a valid option for the preference type, so allow it to go through
            if (foundOption.isPresent()) {
                // an existing preference for the user has already been set, so update the value for it
                if (existingUserPreference.isPresent()) {
                    // only update if the value is actually different
                    if (foundOption.get().getId().compareTo(
                            existingUserPreference.get().getOption().getId()) != 0) {
                        existingUserPreference.get().setOption(foundOption.get());
                        persistedPreferences.add(existingUserPreference.get());
                    }
                } else {
                    UserPreferenceEntity newUserPreference = new UserPreferenceEntity();
                    newUserPreference.setUser(user);
                    newUserPreference.setApplication(application);
                    newUserPreference.setType(preferenceType);
                    newUserPreference.setOption(foundOption.get());
                    persistedPreferences.add(newUserPreference);
                }
            } else {
                throw new ResourceNotFoundException(
                        String.format("No valid option value found for type %s and value %s.",
                                preferenceType.getName(),
                                entry.getValue())
                );
            }
        }

        userPreferencesRepository.saveAll(persistedPreferences);
    }
}
