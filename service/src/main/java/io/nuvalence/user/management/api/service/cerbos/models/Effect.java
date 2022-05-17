package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Generated;

/**
 * The allowed effect values for Cerbos.
 */

@Generated
public enum Effect {
    @JsonProperty("EFFECT_UNSPECIFIED")
    EFFECT_UNSPECIFIED,

    @JsonProperty("EFFECT_ALLOW")
    EFFECT_ALLOW,

    @JsonProperty("EFFECT_DENY")
    EFFECT_DENY,

    @JsonProperty("EFFECT_NO_MATCH")
    EFFECT_NO_MATCH
}
