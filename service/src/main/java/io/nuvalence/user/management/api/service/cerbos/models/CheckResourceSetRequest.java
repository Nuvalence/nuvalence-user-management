package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

/**
 * Check whether a principal has permissions to perform the given actions on a set of resource instances.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@AllArgsConstructor
@Builder
@Getter
@Setter
@Jacksonized
public class CheckResourceSetRequest {
    private String requestId;

    private String[] actions;

    private Principal principal;

    private ResourceSet resource;

    private Boolean includeMeta;

    private RequestAuxData auxData;
}
