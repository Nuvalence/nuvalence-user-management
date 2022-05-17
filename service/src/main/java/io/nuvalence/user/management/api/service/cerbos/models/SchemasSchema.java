package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class SchemasSchema {
    private String ref;

    private SchemasIgnoreWhen ignoreWhen;
}
