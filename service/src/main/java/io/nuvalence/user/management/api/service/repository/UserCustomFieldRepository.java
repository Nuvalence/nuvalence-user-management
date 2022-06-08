package io.nuvalence.user.management.api.service.repository;

import io.nuvalence.user.management.api.service.entity.UserCustomFieldEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserCustomField.
 */

@Repository
public interface UserCustomFieldRepository extends JpaRepository<UserCustomFieldEntity, UUID> {
    @Query(value = "SELECT * FROM user_custom_field WHERE custom_field_id = :customFieldId", nativeQuery = true)
    List<UserCustomFieldEntity> findAllByCustomField(@Param("customFieldId") UUID customFieldId);

    @Query(value = "SELECT * FROM user_custom_field WHERE user_id = :userId", nativeQuery = true)
    List<UserCustomFieldEntity> findAllByUserId(@Param("userId") UUID userId);

    @Query(value = "SELECT * FROM user_custom_field WHERE user_id = :userId AND custom_field_id = :customFieldId",
            nativeQuery = true)
    Optional<UserCustomFieldEntity> findFirstByUserAndCustomField(UUID userId, UUID customFieldId);
}
