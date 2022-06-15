package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.CustomFieldDataTypeEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldOptionEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldTypeEntity;
import io.nuvalence.user.management.api.service.entity.UserCustomFieldEntity;
import io.nuvalence.user.management.api.service.enums.CustomFieldType;
import io.nuvalence.user.management.api.service.generated.models.CreateCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateCustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.UpdateCustomFieldDTO;
import io.nuvalence.user.management.api.service.mapper.CustomFieldMapper;
import io.nuvalence.user.management.api.service.repository.CustomFieldDataTypeRepository;
import io.nuvalence.user.management.api.service.repository.CustomFieldOptionRepository;
import io.nuvalence.user.management.api.service.repository.CustomFieldRepository;
import io.nuvalence.user.management.api.service.repository.CustomFieldTypeRepository;
import io.nuvalence.user.management.api.service.repository.UserCustomFieldRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

/**
 * CustomField Service.
 */

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class CustomFieldService {
    private final UserCustomFieldRepository userCustomFieldRepository;
    private final CustomFieldDataTypeRepository customFieldDataTypeRepository;
    private final CustomFieldTypeRepository customFieldTypeRepository;
    private final CustomFieldRepository customFieldRepository;
    private final CustomFieldOptionRepository customFieldOptionRepository;

    /**
     * Gets all custom fields.
     *
     * @return A list of custom fields.
     */
    public ResponseEntity<List<CustomFieldDTO>> getAllCustomFields() {
        List<CustomFieldEntity> allCustomFields = customFieldRepository.findAll(
                Sort.by(Sort.Direction.ASC, "displayText")
        );
        if (allCustomFields.isEmpty()) {
            throw new ResourceNotFoundException("No custom fields found.");
        }

        List<CustomFieldDTO> customFields = allCustomFields.stream()
                .map(CustomFieldMapper.INSTANCE::convertEntityToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(customFields);
    }

    /**
     * Gets a custom field by its id.
     *
     * @param id the id of the custom field to retrieve.
     * @return a custom field.
     */
    public ResponseEntity<CustomFieldDTO> getCustomFieldById(UUID id) {
        Optional<CustomFieldEntity> customFieldEntity = customFieldRepository.findById(id);
        if (customFieldEntity.isEmpty()) {
            throw new ResourceNotFoundException("Custom field not found!");
        }

        CustomFieldDTO customField = CustomFieldMapper.INSTANCE.convertEntityToDto(customFieldEntity.get());
        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(customField);
    }

    /**
     * Gets the options for a custom field.
     *
     * @param customFieldId the id of the custom field.
     * @return a list of custom field options.
     */
    public ResponseEntity<List<CustomFieldOptionDTO>> getOptionsForCustomField(UUID customFieldId) {
        Optional<CustomFieldEntity> customField = customFieldRepository.findById(customFieldId);
        if (customField.isEmpty()) {
            throw new ResourceNotFoundException("Custom field not found!");
        }

        if (!isDropDownListType(customField.get())) {
            throw new BusinessLogicException("The custom field is not a drop-down type.");
        }

        List<CustomFieldOptionDTO> options = customField.get().getOptions()
                .stream().map(CustomFieldMapper.INSTANCE::convertOptionEntityToOptionDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(options);
    }

    /**
     * Creates a new custom field.
     *
     * @param customField the custom field.
     * @return a status code.
     */
    public ResponseEntity<Void> addCustomField(CreateCustomFieldDTO customField) {
        Optional<CustomFieldEntity> checkName = customFieldRepository.findFirstByName(customField.getName());
        if (checkName.isPresent()) {
            throw new BusinessLogicException("A custom field with this name already exists.");
        }

        Optional<CustomFieldTypeEntity> customFieldType = customFieldTypeRepository
                .findFirstByType(customField.getType().getValue());
        Optional<CustomFieldDataTypeEntity> customFieldDataType = customFieldDataTypeRepository
                .findFirstByType(customField.getDataType().getValue());

        // these should always be found thanks to restricting by enum values, but check regardless
        if (customFieldType.isEmpty()) {
            throw new ResourceNotFoundException("Custom field type not found!");
        }
        if (customFieldDataType.isEmpty()) {
            throw new ResourceNotFoundException("Custom field data type not found!");
        }

        CustomFieldEntity customFieldEntity = CustomFieldEntity.builder()
                .name(customField.getName())
                .type(customFieldType.get())
                .dataType(customFieldDataType.get())
                .displayText(customField.getDisplayText())
                .build();
        CustomFieldEntity savedCustomField = customFieldRepository.save(customFieldEntity);

        if (isDropDownListType(customFieldEntity)
                && customField.getOptions() != null
                && !customField.getOptions().isEmpty()) {
            List<CustomFieldOptionEntity> options = customField.getOptions()
                    .stream().map(o -> CustomFieldMapper.INSTANCE.convertOptionDtoToOptionEntity(o, savedCustomField))
                    .collect(Collectors.toList());
            customFieldOptionRepository.saveAll(options);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Updates an existing custom field.
     *
     * @param customFieldId the id of the custom field.
     * @param customField the custom field.
     * @return a status code.
     */
    public ResponseEntity<Void> updateCustomField(UUID customFieldId, UpdateCustomFieldDTO customField) {
        Optional<CustomFieldEntity> foundCustomField = customFieldRepository.findById(customFieldId);
        if (foundCustomField.isEmpty()) {
            throw new ResourceNotFoundException("Custom field not found!");
        }

        Optional<CustomFieldEntity> checkName = customFieldRepository
                .findFirstByNameAndIdNot(customField.getName(), customFieldId);
        if (checkName.isPresent()) {
            throw new BusinessLogicException("A custom field already exists with that name.");
        }

        foundCustomField.get().setName(customField.getName());
        foundCustomField.get().setDisplayText(customField.getDisplayText());
        CustomFieldEntity savedCustomField = customFieldRepository.save(foundCustomField.get());

        customFieldOptionRepository.deleteAll(foundCustomField.get().getOptions());
        if (isDropDownListType(foundCustomField.get())
                && customField.getOptions() != null
                && !customField.getOptions().isEmpty()) {
            List<CustomFieldOptionEntity> options = customField.getOptions()
                    .stream().map(o -> CustomFieldMapper.INSTANCE.convertOptionDtoToOptionEntity(o, savedCustomField))
                    .collect(Collectors.toList());
            customFieldOptionRepository.saveAll(options);
        }

        return ResponseEntity.ok().build();
    }

    /**
     * Deletes an existing custom field.
     *
     * @param customFieldId the id of the custom field.
     * @return a status code.
     */
    public ResponseEntity<Void> deleteCustomField(UUID customFieldId) {
        Optional<CustomFieldEntity> foundCustomField = customFieldRepository.findById(customFieldId);
        if (foundCustomField.isEmpty()) {
            throw new ResourceNotFoundException("Custom field not found!");
        }

        List<UserCustomFieldEntity> userCustomFields = userCustomFieldRepository.findAllByCustomField(customFieldId);
        userCustomFieldRepository.deleteAll(userCustomFields);
        customFieldOptionRepository.deleteAll(foundCustomField.get().getOptions());
        customFieldRepository.delete(foundCustomField.get());

        return ResponseEntity.ok().build();
    }

    /**
     * Updates the list of options for a custom field.
     *
     * @param customFieldId the id of the custom field.
     * @param options a list of custom field options.
     * @return a status code.
     */
    public ResponseEntity<Void> updateCustomFieldOptions(UUID customFieldId,
                                                         List<CreateOrUpdateCustomFieldOptionDTO> options) {
        if (options == null || options.isEmpty()) {
            throw new BusinessLogicException("No options were passed in.");
        }

        Optional<CustomFieldEntity> customField = customFieldRepository.findById(customFieldId);

        if (customField.isEmpty()) {
            throw new ResourceNotFoundException("Custom field not found!");
        }

        if (!isDropDownListType(customField.get())) {
            throw new BusinessLogicException("The custom field is not a drop-down type.");
        }

        List<CustomFieldOptionEntity> optionEntities = options
                .stream().map(o -> CustomFieldMapper.INSTANCE.convertOptionDtoToOptionEntity(o, customField.get()))
                .collect(Collectors.toList());

        customFieldOptionRepository.deleteAll(customField.get().getOptions());
        customFieldOptionRepository.saveAll(optionEntities);

        return ResponseEntity.ok().build();
    }

    private boolean isDropDownListType(CustomFieldEntity customField) {
        return  CustomFieldType.fromText(customField.getType().getType()) == CustomFieldType.DROP_DOWN_LIST;
    }
}
