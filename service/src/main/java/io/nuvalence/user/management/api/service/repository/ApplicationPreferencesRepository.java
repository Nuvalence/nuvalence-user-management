package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Application Preferences Repo.
 */
@Repository
public interface ApplicationPreferencesRepository extends JpaRepository<UserPreferenceEntity, UUID> {
    //TODO: refactor this to get all user_preference where app_id = passed in id
    //@Query("select a from ApplicationPreferenceEntity a where a.user.id = ?1 and a.application.id = ?2")
    @Query(value = "SELECT * from user_preference WHERE user_id = ?1 AND application_id = ?2", nativeQuery = true)
    Optional<UserPreferenceEntity> findApplicationPreferenceByApplicationId(
            @NonNull UUID userId,
            @NonNull UUID appId
    );
}
