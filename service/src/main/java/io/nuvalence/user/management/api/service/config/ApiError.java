package io.nuvalence.user.management.api.service.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * Helper utility to handle custom REST exceptions.
 */

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class ApiError {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String errorCode;
    private final String message;
}
