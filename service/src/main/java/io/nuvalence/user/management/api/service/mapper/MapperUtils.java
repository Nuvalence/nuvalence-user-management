package io.nuvalence.user.management.api.service.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserCustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceOptionEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceTypeEntity;
import io.nuvalence.user.management.api.service.entity.UserRoleEntity;
import io.nuvalence.user.management.api.service.enums.CustomFieldType;
import io.nuvalence.user.management.api.service.generated.models.AssignedRoleDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldDataType;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleApplicationDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceTypeDTO;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
     * @param roleEntities                      list of role entities
     * @param applicationRolePermissionMappings the mapping of application roles to permissions
     * @param apps                              list of application entities
     * @return list of role dto
     */
    public static List<RoleDTO> mapRoleEntitiesToRoleList(List<RoleEntity> roleEntities,
                                                  Map<String, Map<String, String[]>> applicationRolePermissionMappings,
                                                  List<ApplicationEntity> apps) {
        // Loop over each role entity
        return roleEntities.stream().map(r -> {
            // Convert to RoleDTO
            RoleDTO roleDto = RoleEntityMapper.INSTANCE.roleEntityToRoleDto(r);
            // Create a new list for the applications that have permissions for this role
            List<RoleApplicationDTO> raDTOs = new ArrayList<>();
            // Loop over each application
            for (ApplicationEntity app : apps) {
                // Extract the relevant permissions
                Map<String, String[]> rolePermissionMap = applicationRolePermissionMappings.get(app.getName());
                String[] permissions = rolePermissionMap.get(r.getRoleName());
                if (permissions != null) {
                    // Create and fill RoleApplicationDTOs for each application permission
                    RoleApplicationDTO raDTO = new RoleApplicationDTO();
                    raDTO.setApplicationId(app.getId());
                    raDTO.setName(app.getName());
                    raDTO.setDisplayName(app.getDisplayName());
                    raDTO.setPermissions(Arrays.asList(permissions));
                    raDTOs.add(raDTO);
                }
            }
            // Save the applications to the role and return
            roleDto.setApplications(raDTOs);
            return roleDto;
        }).collect(Collectors.toList());
    }

    /**
     * Simple list mapper for RoleEntities -> AssignedRoleDTOs.
     *
     * @param roleEntities list of RoleEntities
     * @return list of AssignedRoleDTOs
     */
    public static List<AssignedRoleDTO> mapRoleEntitiesToAssignedRoleList(List<RoleEntity> roleEntities) {
        return roleEntities.stream().map(RoleEntityMapper.INSTANCE::roleEntityToAssignedRoleDto)
            .collect(Collectors.toList());
    }

    /**
     * Combines user preference with application-specific preferences.
     *
     * @param userPreference User preferences.
     * @param appPreferences Application preferences.
     * @param userId User ID.
     * @param appId Application ID.
     * @return Updated preferences.
     */
    public static UserPreferenceDTO overlapPreferences(List<UserPreferenceEntity> userPreference,
                                                       List<UserPreferenceEntity> appPreferences,
                                                       UUID userId, UUID appId) {
        List<UserPreferenceEntity> preferences = overlapPreferenceEntities(userPreference, appPreferences);
        UserPreferenceDTO preferenceDto = new UserPreferenceDTO();
        preferenceDto.putAll(preferences.stream().collect(
                Collectors.toMap(p -> p.getType().getName(), p -> p.getOption().getValue())
        ));

        if (!preferences.isEmpty()) {
            preferenceDto.setUserId(preferences.get(0).getUser().getId());
            if (preferences.get(0).getApplication() != null) {
                preferenceDto.setApplicationId(preferences.get(0).getApplication().getId());
            }
        }
        preferenceDto.setUserId(userId);
        preferenceDto.setApplicationId(appId);
        return preferenceDto;
    }

    /**
     * Given two entities, merge appPreferences into userPreferences if appPreference's respective field is not null.
     *
     * @param userPreferences User Preferences.
     * @param appPreferences Application Preferences.
     * @return Merged preferences.
     */
    public static List<UserPreferenceEntity> overlapPreferenceEntities(List<UserPreferenceEntity> userPreferences,
                                                                 List<UserPreferenceEntity> appPreferences) {
        List<UserPreferenceEntity> updatedPreferences = new ArrayList<>(userPreferences);
        appPreferences.forEach(a -> {
            updatedPreferences.removeIf(p -> p.getType().getId().compareTo(a.getType().getId()) == 0);
            updatedPreferences.add(a);
        });

        return updatedPreferences;
    }

    /**
     * Simple list mapper for UserEntities to a list of AssignedRoleDTOs.
     * @param user a UserEntity
     * @return a list of AssignedRoleDTOs
     */
    public static List<AssignedRoleDTO> mapUserEntityToAssignedRoleList(UserEntity user) {
        if (user.getUserRoleEntities() == null) {
            return null;
        }

        return mapRoleEntitiesToAssignedRoleList(user.getUserRoleEntities().stream()
            .map(UserRoleEntity::getRole).collect(Collectors.toList()));
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

    /**
     * Maps between {@link UserPreferenceTypeEntity} to {@link UserPreferenceTypeDTO}.
     *
     * @param type a user preference type entity
     * @return a user preference type dto
     */
    public static UserPreferenceTypeDTO mapUserPreferenceTypeEntityToDto(UserPreferenceTypeEntity type) {
        UserPreferenceTypeDTO typeDto = UserPreferenceTypeEntityMapper.INSTANCE.userPreferenceTypeEntityToDto(type);
        List<UserPreferenceOptionEntity> typeOptions = type.getUserPreferenceOptionEntities();
        if (typeOptions != null && !typeOptions.isEmpty()) {
            List<String> options = typeOptions
                    .stream().map(UserPreferenceOptionEntity::getValue)
                    .collect(Collectors.toList());
            typeDto.setOptions(options);
        } else {
            typeDto.setOptions(Collections.emptyList());
        }

        return typeDto;
    }
}
