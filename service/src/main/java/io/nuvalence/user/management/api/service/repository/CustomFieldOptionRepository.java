package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.CustomFieldOptionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for CustomFieldOption.
 */

@Repository
public interface CustomFieldOptionRepository extends JpaRepository<CustomFieldOptionEntity, UUID> {
    @Query(value = "SELECT * FROM custom_field_option WHERE custom_field_id = :customFieldId", nativeQuery = true)
    List<CustomFieldOptionEntity> findByCustomField(@Param("customFieldId") UUID customFieldId);
}
