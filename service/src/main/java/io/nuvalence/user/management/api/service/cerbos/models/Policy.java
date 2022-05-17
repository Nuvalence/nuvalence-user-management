package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * A principal.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@Jacksonized
public class Policy {
    private String apiVersion;

    private Metadata metadata;

    private Boolean disabled;

    private String description;

    private ResourcePolicy resourcePolicy;

    private PrincipalPolicy principalPolicy;

    private DerivedRoles derivedRoles;

    private Map<String, String> variables;
}
