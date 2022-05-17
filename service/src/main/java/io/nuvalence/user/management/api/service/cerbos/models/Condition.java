package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

/**
 * Condition on which to apply the policy.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Condition {
    private Match match;

    private String script;
}
