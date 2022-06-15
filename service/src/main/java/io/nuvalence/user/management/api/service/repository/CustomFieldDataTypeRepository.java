package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.CustomFieldDataTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for CustomFieldDataType.
 */

@Repository
public interface CustomFieldDataTypeRepository extends JpaRepository<CustomFieldDataTypeEntity, UUID> {
    @Query(value = "SELECT * FROM custom_field_data_type WHERE type = :type", nativeQuery = true)
    Optional<CustomFieldDataTypeEntity> findFirstByType(@Param("type") String type);
}
