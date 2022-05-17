package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for applications.
 */
@Repository
public interface ApplicationRepository extends JpaRepository<ApplicationEntity, UUID> {

    public Optional<ApplicationEntity> getApplicationById(UUID id);

}
