package io.nuvalence.user.management.api.service.entity;

import com.vladmihalcea.hibernate.type.json.JsonType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

import java.time.OffsetDateTime;
import java.util.UUID;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/**
 * Represents a single UserCustomField.
 */
@Getter
@Setter
@Builder(toBuilder = true)
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_custom_field")
@TypeDefs({
        @TypeDef(name = "json", typeClass = JsonType.class)
})
public class UserCustomFieldEntity {
    @Id
    @GeneratedValue(generator = "uuid2")
    @GenericGenerator(name = "uuid2", strategy = "org.hibernate.id.UUIDGenerator")
    @Type(type = "uuid-char")
    @Column(name = "id", length = 36, insertable = false, updatable = false, nullable = false)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "custom_field_id")
    private CustomFieldEntity customField;

    @Column(name = "custom_field_value_string", nullable = true, length = 255)
    private String customFieldValueString;

    @Column(name = "custom_field_value_int", nullable = true)
    private Integer customFieldValueInt;

    @Type(type = "json")
    @Column(name = "custom_field_value_json", nullable = true, columnDefinition = "json")
    private String customFieldValueJson;

    @Column(name = "custom_field_value_datetime", nullable = true)
    private OffsetDateTime customFieldValueDateTime;
}
