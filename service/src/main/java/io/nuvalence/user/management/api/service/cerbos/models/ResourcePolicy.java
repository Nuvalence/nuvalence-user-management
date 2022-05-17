package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

/**
 * A resource policy.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@Jacksonized
public class ResourcePolicy {
    @Builder.Default
    private String resource = "default";

    @Builder.Default
    private String version = "default";

    private String[] importDerivedRoles;

    private ResourceRule[] rules;

    private String scope;

    private Schemas schemas;
}
