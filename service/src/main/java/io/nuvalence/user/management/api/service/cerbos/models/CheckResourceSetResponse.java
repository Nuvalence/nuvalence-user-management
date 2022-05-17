package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

/**
 * Policy evaluation response for a set of resources.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
@Builder
@Jacksonized
public class CheckResourceSetResponse {
    private String requestId;

    private Map<String, CheckResourceSetResponseActionEffectMap> resourceInstances;

    private CheckResourceSetResponseMeta meta;
}
