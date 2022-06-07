package io.nuvalence.user.management.api.service.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Type;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;



/**
 * Represents a single User Entity.
 */

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_table")
public class UserEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @Type(type = "uuid-char")
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "external_id")
    private String externalId;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "created_At", nullable = false)
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "user")
    private List<UserRoleEntity> userRoleEntities;

    @OneToMany(mappedBy = "user")
    private List<UserCustomFieldEntity> customFields;
}
