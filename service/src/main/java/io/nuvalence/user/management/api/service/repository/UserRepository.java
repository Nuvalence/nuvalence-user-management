package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository for User.
 */

@Repository
public interface UserRepository extends JpaRepository<UserEntity, UUID> {

    Optional<UserEntity> findById(UUID id);

    @Query(value = "SELECT * FROM user_table WHERE external_id = :externalId", nativeQuery = true)
    Optional<UserEntity> findUserEntityByExternalId(@Param("externalId") String externalId);

    @Query(value = "SELECT * FROM user_table WHERE email = :email", nativeQuery = true)
    Optional<UserEntity> findUserEntityByEmail(@Param("email") String email);

    @Query(value = "SELECT * FROM user_table WHERE display_name = :name", nativeQuery = true)
    Optional<UserEntity> findUserEntityByDisplayName(@Param("name") String displayName);
}
