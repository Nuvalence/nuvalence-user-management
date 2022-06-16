package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Permission.
 */

@Repository
public interface PermissionRepository extends JpaRepository<PermissionEntity, UUID> {

    @Query(value = "SELECT * FROM permission where name = :name", nativeQuery = true)
    Optional<PermissionEntity> findByPermissionName(@Param("name") String name);
}