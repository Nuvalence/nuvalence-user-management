package io.nuvalence.user.management.api.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserCustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import io.nuvalence.user.management.api.service.enums.CustomFieldType;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldDataType;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Simple Mapping utility class.
 */

@Slf4j
public class MapperUtils {

    public static List<PermissionDTO> mapPermissionEntitiesToPermissionList(List<PermissionEntity> permissionEntities) {
        return permissionEntities.stream().map(PermissionEntityMapper.INSTANCE::permissionEntityToPermissionDto)
                .collect(Collectors.toList());
    }

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
                                                       UserPreferenceEntity appPreferences) {
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
                                                                 UserPreferenceEntity appPreferences) {
        UserPreferenceEntity updatedPreferences = new UserPreferenceEntity();
        updatedPreferences.setId(userPreference.getId());
        //updatedPreferences.setCommunicationPreference(userPreference.getCommunicationPreference());
        //updatedPreferences.setLanguage(
        //  (appPreferences.getLanguage() != null) ? appPreferences.getLanguage() : userPreference.getLanguage()
        //);

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

    /**
     * Simple list mapper for {@link UserCustomFieldEntity} and {@link UserCustomFieldDTO}.
     *
     * @param user the user
     * @return a list of custom fields
     */
    public static List<UserCustomFieldDTO> mapUserEntityToCustomFieldDtoList(UserEntity user) {
        if (user.getCustomFields() == null) {
            return null;
        }

        return user.getCustomFields()
                .stream().map(MapperUtils::mapUserCustomFieldEntityToDto)
                .collect(Collectors.toList());
    }

    /**
     * Maps between {@link UserCustomFieldEntity} and
     * {@link UserCustomFieldDTO}.
     *
     * @param userCustomField the user custom field.
     * @return a dto version of the user custom field.
     */
    public static UserCustomFieldDTO mapUserCustomFieldEntityToDto(UserCustomFieldEntity userCustomField) {
        UserCustomFieldDTO dto = new UserCustomFieldDTO();
        dto.setId(userCustomField.getId());
        dto.setCustomFieldId(userCustomField.getCustomField().getId());
        dto.setType(UserCustomFieldDTO.TypeEnum.fromValue(
                userCustomField.getCustomField().getType().getType()
        ));
        dto.setDataType(UserCustomFieldDTO.DataTypeEnum.fromValue(
                userCustomField.getCustomField().getDataType().getType()
        ));
        dto.setName(userCustomField.getCustomField().getName());
        dto.setDisplayText(userCustomField.getCustomField().getDisplayText());

        switch (CustomFieldDataType.fromValue(userCustomField.getCustomField().getDataType().getType())) {
            case INT:
                dto.setValue(userCustomField.getCustomFieldValueInt());
                break;
            case JSON:
                if (userCustomField.getCustomFieldValueJson() != null) {
                    try {
                        dto.setValue(
                                new ObjectMapper().convertValue(userCustomField.getCustomFieldValueJson(),Object.class)
                        );
                    } catch (IllegalArgumentException e) {
                        log.error("Exception occurred in MapperUtils.mapUserCustomFieldEntityToDto: {}",
                                e.getMessage());
                    }
                }
                break;
            case DATETIME:
                dto.setValue(userCustomField.getCustomFieldValueDateTime());
                break;
            case STRING:
            default:
                dto.setValue(userCustomField.getCustomFieldValueString());
                break;
        }

        // only populate options if type is drop-down list
        if (CustomFieldType.fromText(userCustomField.getCustomField().getType().getType())
                == CustomFieldType.DROP_DOWN_LIST
                && userCustomField.getCustomField().getOptions() != null
                && userCustomField.getCustomField().getOptions().size() > 0) {
            dto.setOptions(userCustomField.getCustomField().getOptions()
                    .stream().map(CustomFieldMapper.INSTANCE::convertOptionEntityToOptionDto)
                    .collect(Collectors.toList())
            );
        }

        return dto;
    }
}
