package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for applications.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {
    @Query(value = "SELECT * FROM application WHERE app_name = :name", nativeQuery = true)
    Optional<ApplicationEntity> getApplicationByName(@Param("name") String name);
}
