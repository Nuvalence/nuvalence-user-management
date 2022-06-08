package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.CustomFieldTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CustomFieldType.
 */

@Repository
public interface CustomFieldTypeRepository extends JpaRepository<CustomFieldTypeEntity, UUID> {
    @Query(value = "SELECT * FROM custom_field_type WHERE type = :type", nativeQuery = true)
    Optional<CustomFieldTypeEntity> findFirstByType(@Param("type") String type);
}
