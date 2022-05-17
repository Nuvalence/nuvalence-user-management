package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * Repository for UserRole.
 */

@Repository
public interface UserRoleRepository extends JpaRepository<UserRoleEntity, UUID> {

    List<UserRoleEntity> findAllByUserId(UUID userId);

    List<UserRoleEntity> findAllByRoleId(UUID roleId);

    UserRoleEntity findByUserAndRole(UserEntity user, RoleEntity role);

}
