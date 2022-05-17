package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.ApplicationPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple Mapping utility class.
 */
public class MapperUtils {

    /**
     * Simple list mapper for entity -> dto.
     *
     * @param roleEntities list of role entities
     * @return list of role dto
     */
    public static List<RoleDTO> mapRoleEntitiesToRoleList(List<RoleEntity> roleEntities) {
        return roleEntities.stream().map(RoleEntityMapper.INSTANCE::roleEntityToRoleDto)
                .collect(Collectors.toList());
    }

    /**
     * Simple list mapper for entity -> dto.
     *
     * @param roleEntities           list of role entities
     * @param rolePermissionMappings the mapping of roles to permissions
     * @return list of role dto
     */
    public static List<RoleDTO> mapRoleEntitiesToRoleList(List<RoleEntity> roleEntities,
                                                          Map<String, String[]> rolePermissionMappings) {
        return roleEntities.stream().map(r -> {
            RoleDTO roleDto = RoleEntityMapper.INSTANCE.roleEntityToRoleDto(r);
            String[] permissions = rolePermissionMappings.get(r.getRoleName());
            if (permissions != null) {
                roleDto.setPermissions(Arrays.asList(permissions));
            } else {
                roleDto.setPermissions(Collections.emptyList());
            }
            return roleDto;
        }).collect(Collectors.toList());
    }

    /**
     * Combines user preference with application-specific preferences.
     *
     * @param userPreference User preferences.
     * @param appPreferences Application preferences.
     * @return Updated preferences.
     */
    public static UserPreferenceDTO overlapPreferences(UserPreferenceEntity userPreference,
                                                       ApplicationPreferenceEntity appPreferences) {
        return UserPreferenceEntityMapper.INSTANCE.userPreferencesEntityToDto(
                overlapPreferenceEntities(userPreference, appPreferences)
        );
    }

    /**
     * Given two entities, merge appPreferences into userPreference if appPreference's respective field is not null.
     *
     * @param userPreference User Preferences.
     * @param appPreferences Application Preferences.
     * @return Merged preferences.
     */
    public static UserPreferenceEntity overlapPreferenceEntities(UserPreferenceEntity userPreference,
                                                                 ApplicationPreferenceEntity appPreferences) {
        UserPreferenceEntity updatedPreferences = new UserPreferenceEntity();
        updatedPreferences.setId(userPreference.getId());
        updatedPreferences.setCommunicationPreference(userPreference.getCommunicationPreference());
        updatedPreferences.setLanguage(
                (appPreferences.getLanguage() != null) ? appPreferences.getLanguage() : userPreference.getLanguage()
        );

        return updatedPreferences;
    }



    /**
     * Simple list mapper for user => role list.
     * @param user the user
     * @return a list of roles
     */
    public static List<RoleDTO> mapUserEntityToRoleList(UserEntity user) {
        if (user.getUserRoleEntities() == null) {
            return null;
        }

        return mapRoleEntitiesToRoleList(user.getUserRoleEntities().stream()
                .map(UserRoleEntity::getRole).collect(Collectors.toList()));
    }

    /**
     * Simple list mapper for user => role list.
     * @param user the user
     * @param rolePermissionMappings the mapping of roles to permissions
     * @return a list of roles
     */
    public static List<RoleDTO> mapUserEntityToRoleList(UserEntity user,
                                                        Map<String, String[]> rolePermissionMappings) {

        if (user.getUserRoleEntities() == null) {
            return null;
        }

        return mapRoleEntitiesToRoleList(user.getUserRoleEntities().stream()
                .map(UserRoleEntity::getRole).collect(Collectors.toList()), rolePermissionMappings);
    }
}
