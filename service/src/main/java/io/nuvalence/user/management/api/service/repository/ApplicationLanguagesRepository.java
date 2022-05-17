package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.ApplicationLanguageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Represents the Application Language Repo.
 */
@Repository
public interface ApplicationLanguagesRepository extends JpaRepository<ApplicationLanguageEntity, UUID> {

    List<ApplicationLanguageEntity> findLanguagesByApplicationId(UUID applicationId);

}
