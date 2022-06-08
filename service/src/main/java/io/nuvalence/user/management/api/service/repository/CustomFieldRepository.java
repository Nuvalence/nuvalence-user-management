package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.CustomFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CustomField.
 */

@Repository
public interface CustomFieldRepository extends JpaRepository<CustomFieldEntity, UUID> {
    @Query(value = "SELECT * FROM custom_field WHERE name = :name", nativeQuery = true)
    Optional<CustomFieldEntity> findFirstByName(@Param("name") String name);
}
