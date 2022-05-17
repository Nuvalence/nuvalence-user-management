package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * A person or application attempting to perform the actions on the set of resources.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@Jacksonized
public class Principal {
    private String id;

    private String policyVersion;

    private String[] roles;

    private Map<String, Object> attr;

    private String scope;
}
