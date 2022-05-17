package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

/**
 * Repository for Role.
 */

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, UUID> {

    RoleEntity findByRoleName(String name);
}
