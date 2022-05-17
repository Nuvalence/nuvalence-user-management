package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

/**
 *  JWT from the original request.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class AuxDataJwt {
    private String token;

    private String keySetId;
}
