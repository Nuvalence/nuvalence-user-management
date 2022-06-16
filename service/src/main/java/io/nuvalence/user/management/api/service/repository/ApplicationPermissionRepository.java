package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.ApplicationPermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for ApplicationPermission.
 */

@Repository
public interface ApplicationPermissionRepository extends JpaRepository<ApplicationPermissionEntity, UUID> {

    @Query(value = "SELECT * FROM application_permission WHERE permission_id = :permissionId", nativeQuery = true)
    List<ApplicationPermissionEntity> findByPermissionId(@Param("permissionId") UUID permissionId);
}