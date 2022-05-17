package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

/**
 * Derived roles that were effective during policy evaluation.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class DerivedRoles {
    private String name;

    private RoleDef[] definitions;
}
