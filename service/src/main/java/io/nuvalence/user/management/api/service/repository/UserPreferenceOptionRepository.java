package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.UserPreferenceOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * User Preference Options Repository.
 */
@Repository
public interface UserPreferenceOptionRepository extends JpaRepository<UserPreferenceOptionEntity, UUID> {
}
