package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Metadata about resource instances.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class MetaActionMeta {
    private Map<String, MetaEffectMeta> actions;

    private String[] effectiveDerivedRoles;
}
