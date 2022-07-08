package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.UserPreferenceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * User Preferences Type Repository.
 */

@Repository
public interface UserPreferenceTypeRepository extends JpaRepository<UserPreferenceTypeEntity, UUID> {
    @Query(value = "SELECT * FROM user_preference_type WHERE name IN (:names)", nativeQuery = true)
    List<UserPreferenceTypeEntity> findAllByNames(@Param("names") List<String> names);
}
