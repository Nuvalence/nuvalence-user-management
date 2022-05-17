package io.nuvalence.user.management.api.service.cerbos.models;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Generated;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * Metadata for a Policy.
 */

@Generated
@JsonInclude(JsonInclude.Include.NON_NULL)
@Getter
@Setter
public class Metadata {
    private String sourceFile;

    private Map<String, String> annotations;

    private String hash;

    private String storeIdentifier;
}
