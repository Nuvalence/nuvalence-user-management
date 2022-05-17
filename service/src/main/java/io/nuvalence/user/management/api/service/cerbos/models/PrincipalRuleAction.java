package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

/**
 * An individual action and effect (permission).
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class PrincipalRuleAction {
    private String action;

    private Condition condition;

    private Effect effect;

    private String type;
}
