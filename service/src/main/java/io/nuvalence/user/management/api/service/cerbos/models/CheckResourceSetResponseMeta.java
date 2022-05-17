package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Optional metadata about the request evaluation process.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class CheckResourceSetResponseMeta {
    private Map<String, MetaActionMeta> resourceInstances;
}
