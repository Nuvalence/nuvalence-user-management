package io.nuvalence.user.management.api.service.interfaces;

import io.nuvalence.user.management.api.service.entity.UserEntity;

import java.util.Map;

/**
 * Interface for classes that will implement permission checks.
 */

public interface Permissionable {
    Boolean check(String resourceName, UserEntity userEntity, String... permissionsToCheck);

    Boolean updateRolePermissionMappings(String resourceName, String roleName, String[] permissions);

    Boolean removeRole(String resourceName, String roleName);

    Map<String, String[]> getRolePermissionMappings(String resourceName);
}
