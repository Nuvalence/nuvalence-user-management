package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * User Preferences Repository.
 */
@Repository
public interface UserPreferencesRepository extends JpaRepository<UserPreferenceEntity, UUID> {

    @Query(value = "select * from user_preference where user_id = :userId and application_id is null",
            nativeQuery = true)
    List<UserPreferenceEntity> findPreferencesByUserId(@Param("userId") UUID userId);

    @Query(value = "select * from user_preference where user_id = :userId and application_id = :appId", nativeQuery =
            true)
    List<UserPreferenceEntity> findUserApplicationPreferences(@Param("userId") UUID userId,
                                                              @Param("appId") UUID appId);
}
