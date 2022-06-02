package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.UserPreferenceTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * User Preferences Type Repository.
 */

@Repository
public interface UserPreferenceTypeRepository extends JpaRepository<UserPreferenceTypeEntity, UUID> {

}
