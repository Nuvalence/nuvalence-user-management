package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.BusinessLogicException;
import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.CustomFieldDataTypeEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldOptionEntity;
import io.nuvalence.user.management.api.service.entity.CustomFieldTypeEntity;
import io.nuvalence.user.management.api.service.entity.UserCustomFieldEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateCustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.UpdateCustomFieldDTO;
import io.nuvalence.user.management.api.service.repository.CustomFieldDataTypeRepository;
import io.nuvalence.user.management.api.service.repository.CustomFieldOptionRepository;
import io.nuvalence.user.management.api.service.repository.CustomFieldRepository;
import io.nuvalence.user.management.api.service.repository.CustomFieldTypeRepository;
import io.nuvalence.user.management.api.service.repository.UserCustomFieldRepository;
import org.assertj.core.util.IterableUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomFieldServiceTest {
    @Mock
    private UserCustomFieldRepository userCustomFieldRepository;

    @Mock
    private CustomFieldDataTypeRepository customFieldDataTypeRepository;

    @Mock
    private CustomFieldTypeRepository customFieldTypeRepository;

    @Mock
    private CustomFieldRepository customFieldRepository;

    @Mock
    private CustomFieldOptionRepository customFieldOptionRepository;

    @InjectMocks
    private CustomFieldService customFieldService;

    @Captor
    private ArgumentCaptor<CustomFieldEntity> customFieldCaptor;

    @Captor
    private ArgumentCaptor<Iterable<CustomFieldOptionEntity>> customFieldOptionListCaptor;

    @Captor
    private ArgumentCaptor<Iterable<UserCustomFieldEntity>> userCustomFieldListCaptor;

    @Test
    public void getAllCustomFields_succeeds_if_valid() {
        List<CustomFieldEntity> customFields = List.of(getCustomFieldEntity());
        when(customFieldRepository.findAll(Sort.by(Sort.Direction.ASC, "displayText")))
                .thenReturn(customFields);

        ResponseEntity<List<CustomFieldDTO>> res = customFieldService.getAllCustomFields();
        assertEquals(res.getStatusCode(), HttpStatus.OK);

        assertEquals(Objects.requireNonNull(res.getBody()).size(), customFields.size());
        assertEquals(res.getBody().get(0).getName(), customFields.get(0).getName());
    }

    @Test
    public void getAllCustomField_fails_if_empty_list() {
        when(customFieldRepository.findAll(Sort.by(Sort.Direction.ASC, "displayText")))
                .thenReturn(Collections.emptyList());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                customFieldService.getAllCustomFields());
        assertEquals(exception.getMessage(), "No custom fields found.");
    }

    @Test
    public void getCustomFieldById_succeeds_if_field_exists() {
        CustomFieldEntity customField = getCustomFieldEntity();
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customField));

        ResponseEntity<CustomFieldDTO> res = customFieldService.getCustomFieldById(UUID.randomUUID());

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(Objects.requireNonNull(res.getBody()).getName(), customField.getName());
    }

    @Test
    public void getCustomFieldById_fails_if_field_does_not_exist() {
        when(customFieldRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                customFieldService.getCustomFieldById(UUID.randomUUID()));
        assertEquals(exception.getMessage(), "Custom field not found!");
    }

    @Test
    public void getOptionsForCustomField_succeeds_if_field_is_dropDown() {
        CustomFieldEntity customField = getCustomFieldEntity();
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customField));

        ResponseEntity<List<CustomFieldOptionDTO>> res = customFieldService.getOptionsForCustomField(UUID.randomUUID());

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(Objects.requireNonNull(res.getBody()).size(), customField.getOptions().size());
        assertEquals(res.getBody().get(0).getOptionValue(), customField.getOptions().get(0).getOptionValue());
    }

    @Test
    public void getOptionsForCustomField_fails_if_field_does_not_exist() {
        when(customFieldRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                customFieldService.getOptionsForCustomField(UUID.randomUUID()));
        assertEquals(exception.getMessage(), "Custom field not found!");
    }

    @Test
    public void getOptionsForCustomField_fails_if_field_is_not_dropdown() {
        CustomFieldEntity customField = getCustomFieldEntity();
        customField.setType(getTextFieldTypeEntity());
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customField));

        Exception exception = assertThrows(BusinessLogicException.class, () ->
                customFieldService.getOptionsForCustomField(UUID.randomUUID())
        );
        assertEquals(exception.getMessage(), "The custom field is not a drop-down type.");
    }

    @Test
    public void addCustomField_succeeds_if_valid_dropDownType() {
        CreateCustomFieldDTO customField = getCreateCustomFieldDto(CreateCustomFieldDTO.TypeEnum.DROP_DOWN_LIST,
                CreateCustomFieldDTO.DataTypeEnum.STRING);
        when(customFieldRepository.findFirstByName(anyString())).thenReturn(Optional.empty());
        when(customFieldTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getDropDownTypeEntity()));
        when(customFieldDataTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getStringDataTypeEntity()));

        ResponseEntity<Void> res = customFieldService.addCustomField(customField);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(customFieldRepository).save(customFieldCaptor.capture());
        CustomFieldEntity savedCustomField = customFieldCaptor.getValue();
        assertEquals(savedCustomField.getName(), customField.getName());

        verify(customFieldOptionRepository).saveAll(customFieldOptionListCaptor.capture());
        Iterable<CustomFieldOptionEntity> savedCustomFieldOptions = customFieldOptionListCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(savedCustomFieldOptions), customField.getOptions().size());
    }

    @Test
    public void addCustomField_succeeds_if_valid_textField_string() {
        CreateCustomFieldDTO customField = getCreateCustomFieldDto(CreateCustomFieldDTO.TypeEnum.TEXT_FIELD,
                CreateCustomFieldDTO.DataTypeEnum.STRING);
        when(customFieldRepository.findFirstByName(anyString())).thenReturn(Optional.empty());
        when(customFieldTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getTextFieldTypeEntity()));
        when(customFieldDataTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getStringDataTypeEntity()));

        ResponseEntity<Void> res = customFieldService.addCustomField(customField);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(customFieldRepository).save(customFieldCaptor.capture());
        CustomFieldEntity savedCustomField = customFieldCaptor.getValue();
        assertEquals(savedCustomField.getName(), customField.getName());

        verify(customFieldOptionRepository, never()).saveAll(customFieldOptionListCaptor.capture());
    }

    @Test
    public void addCustomField_succeeds_if_valid_textField_json() {
        CreateCustomFieldDTO customField = getCreateCustomFieldDto(CreateCustomFieldDTO.TypeEnum.TEXT_FIELD,
                CreateCustomFieldDTO.DataTypeEnum.JSON);
        when(customFieldRepository.findFirstByName(anyString())).thenReturn(Optional.empty());
        when(customFieldTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getTextFieldTypeEntity()));
        when(customFieldDataTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getJsonDataTypeEntity()));

        ResponseEntity<Void> res = customFieldService.addCustomField(customField);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(customFieldRepository).save(customFieldCaptor.capture());
        CustomFieldEntity savedCustomField = customFieldCaptor.getValue();
        assertEquals(savedCustomField.getName(), customField.getName());

        verify(customFieldOptionRepository, never()).saveAll(customFieldOptionListCaptor.capture());
    }

    @Test
    public void addCustomField_succeeds_if_valid_textField_dateTime() {
        CreateCustomFieldDTO customField = getCreateCustomFieldDto(CreateCustomFieldDTO.TypeEnum.TEXT_FIELD,
                CreateCustomFieldDTO.DataTypeEnum.DATETIME);
        when(customFieldRepository.findFirstByName(anyString())).thenReturn(Optional.empty());
        when(customFieldTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getTextFieldTypeEntity()));
        when(customFieldDataTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getDateTimeDataTypeEntity()));

        ResponseEntity<Void> res = customFieldService.addCustomField(customField);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(customFieldRepository).save(customFieldCaptor.capture());
        CustomFieldEntity savedCustomField = customFieldCaptor.getValue();
        assertEquals(savedCustomField.getName(), customField.getName());

        verify(customFieldOptionRepository, never()).saveAll(customFieldOptionListCaptor.capture());
    }

    @Test
    public void addCustomField_succeeds_if_valid_textField_int() {
        CreateCustomFieldDTO customField = getCreateCustomFieldDto(CreateCustomFieldDTO.TypeEnum.TEXT_FIELD,
                CreateCustomFieldDTO.DataTypeEnum.INT);
        when(customFieldRepository.findFirstByName(anyString())).thenReturn(Optional.empty());
        when(customFieldTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getTextFieldTypeEntity()));
        when(customFieldDataTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getIntDataTypeEntity()));

        ResponseEntity<Void> res = customFieldService.addCustomField(customField);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(customFieldRepository).save(customFieldCaptor.capture());
        CustomFieldEntity savedCustomField = customFieldCaptor.getValue();
        assertEquals(savedCustomField.getName(), customField.getName());

        verify(customFieldOptionRepository, never()).saveAll(customFieldOptionListCaptor.capture());
    }

    @Test
    public void addCustomField_fails_if_name_taken() {
        CreateCustomFieldDTO customField = getCreateCustomFieldDto(CreateCustomFieldDTO.TypeEnum.DROP_DOWN_LIST,
                CreateCustomFieldDTO.DataTypeEnum.STRING);
        when(customFieldRepository.findFirstByName(anyString())).thenReturn(Optional.of(getCustomFieldEntity()));

        Exception exception = assertThrows(BusinessLogicException.class, () ->
                customFieldService.addCustomField(customField));

        assertEquals(exception.getMessage(), "A custom field with this name already exists.");
    }

    @Test
    public void addCustomField_fails_if_type_invalid() {
        CreateCustomFieldDTO customField = getCreateCustomFieldDto(CreateCustomFieldDTO.TypeEnum.DROP_DOWN_LIST,
                CreateCustomFieldDTO.DataTypeEnum.STRING);
        when(customFieldTypeRepository.findFirstByType(any())).thenReturn(Optional.empty());
        when(customFieldDataTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getStringDataTypeEntity()));

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                customFieldService.addCustomField(customField));

        assertEquals(exception.getMessage(), "Custom field type not found!");
    }

    @Test
    public void addCustomField_fails_if_dataType_invalid() {
        CreateCustomFieldDTO customField = getCreateCustomFieldDto(CreateCustomFieldDTO.TypeEnum.DROP_DOWN_LIST,
                CreateCustomFieldDTO.DataTypeEnum.STRING);
        when(customFieldTypeRepository.findFirstByType(any())).thenReturn(Optional.of(getDropDownTypeEntity()));
        when(customFieldDataTypeRepository.findFirstByType(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                customFieldService.addCustomField(customField));

        assertEquals(exception.getMessage(), "Custom field data type not found!");
    }

    @Test
    public void updateCustomField_succeeds_if_valid_dropdown() {
        UpdateCustomFieldDTO customField = getUpdateCustomFieldDto();
        CustomFieldEntity customFieldEntity = getCustomFieldEntity();
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customFieldEntity));

        ResponseEntity<Void> res = customFieldService.updateCustomField(customFieldEntity.getId(), customField);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(customFieldRepository).save(customFieldCaptor.capture());
        CustomFieldEntity savedCustomField = customFieldCaptor.getValue();
        assertEquals(savedCustomField.getName(), customField.getName());
        verify(customFieldOptionRepository).deleteAll(any());
        verify(customFieldOptionRepository).saveAll(customFieldOptionListCaptor.capture());
        Iterable<CustomFieldOptionEntity> savedOptions = customFieldOptionListCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(savedOptions), customField.getOptions().size());
    }

    @Test
    public void updateCustomField_succeeds_if_valid_non_dropdown() {
        CustomFieldEntity customFieldEntity = getCustomFieldEntity();
        customFieldEntity.setType(getTextFieldTypeEntity());
        customFieldEntity.setOptions(Collections.emptyList());
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customFieldEntity));
        UpdateCustomFieldDTO customField = getUpdateCustomFieldDto();

        ResponseEntity<Void> res = customFieldService.updateCustomField(customFieldEntity.getId(), customField);

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(customFieldRepository).save(customFieldCaptor.capture());
        CustomFieldEntity savedCustomField = customFieldCaptor.getValue();
        assertEquals(savedCustomField.getName(), customField.getName());

        verify(customFieldOptionRepository).deleteAll(any());
        verify(customFieldOptionRepository, never()).saveAll(customFieldOptionListCaptor.capture());
    }

    @Test
    public void updateCustomField_fails_if_field_does_not_exist() {
        UpdateCustomFieldDTO customField = getUpdateCustomFieldDto();
        CustomFieldEntity customFieldEntity = getCustomFieldEntity();
        when(customFieldRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                customFieldService.updateCustomField(customFieldEntity.getId(), customField));
        assertEquals(exception.getMessage(), "Custom field not found!");
    }

    @Test
    public void updateCustom_field_fails_if_name_taken() {
        UpdateCustomFieldDTO customField = getUpdateCustomFieldDto();
        CustomFieldEntity customFieldEntity = getCustomFieldEntity();
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customFieldEntity));
        when(customFieldRepository.findFirstByNameAndIdNot(anyString(), any()))
                .thenReturn(Optional.of(customFieldEntity));

        Exception exception = assertThrows(BusinessLogicException.class, () ->
                customFieldService.updateCustomField(customFieldEntity.getId(), customField));
        assertEquals(exception.getMessage(), "A custom field already exists with that name.");
    }

    @Test
    public void deleteCustomField_succeeds_if_field_exists_non_dropdown() {
        CustomFieldEntity customFieldEntity = getCustomFieldEntity();
        customFieldEntity.setType(getTextFieldTypeEntity());
        customFieldEntity.setOptions(Collections.emptyList());
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customFieldEntity));
        when(userCustomFieldRepository.findAllByCustomField(any())).thenReturn(List.of(getUserCustomFieldEntity()));

        ResponseEntity<Void> res = customFieldService.deleteCustomField(customFieldEntity.getId());

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userCustomFieldRepository).deleteAll(any());
        verify(customFieldOptionRepository).deleteAll(any());
        verify(customFieldRepository).delete(customFieldCaptor.capture());
        CustomFieldEntity customField = customFieldCaptor.getValue();
        assertEquals(customField.getId(), customFieldEntity.getId());
    }

    @Test
    public void deleteCustomField_succeeds_if_field_exists_dropdown() {
        CustomFieldEntity customFieldEntity = getCustomFieldEntity();
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customFieldEntity));
        when(userCustomFieldRepository.findAllByCustomField(any())).thenReturn(List.of(getUserCustomFieldEntity()));

        ResponseEntity<Void> res = customFieldService.deleteCustomField(customFieldEntity.getId());

        assertEquals(res.getStatusCode(), HttpStatus.OK);
        verify(userCustomFieldRepository).deleteAll(any());
        verify(customFieldOptionRepository).deleteAll(any());
        verify(customFieldRepository).delete(customFieldCaptor.capture());
        CustomFieldEntity customField = customFieldCaptor.getValue();
        assertEquals(customField.getId(), customFieldEntity.getId());
    }

    @Test
    public void deleteCustomField_fails_if_field_does_not_exist() {
        when(customFieldRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                customFieldService.deleteCustomField(UUID.randomUUID()));
        assertEquals(exception.getMessage(), "Custom field not found!");
    }

    @Test
    public void updateCustomFieldOptions_succeeds_if_valid() {
        CustomFieldEntity customField = getCustomFieldEntity();
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customField));
        List<CreateOrUpdateCustomFieldOptionDTO> options = List.of(getCreateOrUpdateCustomFieldOptionDto());

        ResponseEntity<Void> res = customFieldService.updateCustomFieldOptions(customField.getId(), options);

        assertEquals(res.getStatusCode(), HttpStatus.OK);

        verify(customFieldOptionRepository).deleteAll(any());
        verify(customFieldOptionRepository).saveAll(customFieldOptionListCaptor.capture());
        Iterable<CustomFieldOptionEntity> savedCustomFieldOptions = customFieldOptionListCaptor.getValue();
        assertEquals(IterableUtil.sizeOf(savedCustomFieldOptions), options.size());
    }

    @Test
    public void updateCustomFieldOptions_fails_if_no_options() {
        Exception exception = assertThrows(BusinessLogicException.class, () ->
                customFieldService.updateCustomFieldOptions(UUID.randomUUID(), Collections.emptyList()));
        assertEquals(exception.getMessage(), "No options were passed in.");
    }

    @Test
    public void updateCustomFieldOptions_fails_if_field_does_not_exist() {
        when(customFieldRepository.findById(any())).thenReturn(Optional.empty());
        List<CreateOrUpdateCustomFieldOptionDTO> options = List.of(getCreateOrUpdateCustomFieldOptionDto());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                customFieldService.updateCustomFieldOptions(UUID.randomUUID(), options));
        assertEquals(exception.getMessage(), "Custom field not found!");
    }

    @Test
    public void updateCustomFieldOptions_fails_if_field_is_not_dropDown() {
        CustomFieldEntity customField = getCustomFieldEntity();
        customField.setType(getTextFieldTypeEntity());
        when(customFieldRepository.findById(any())).thenReturn(Optional.of(customField));
        List<CreateOrUpdateCustomFieldOptionDTO> options = List.of(getCreateOrUpdateCustomFieldOptionDto());

        Exception exception = assertThrows(BusinessLogicException.class, () ->
                customFieldService.updateCustomFieldOptions(UUID.randomUUID(), options));
        assertEquals(exception.getMessage(), "The custom field is not a drop-down type.");
    }

    private UserCustomFieldEntity getUserCustomFieldEntity() {
        CustomFieldOptionEntity option = new CustomFieldOptionEntity();
        option.setId(UUID.randomUUID());
        option.setOptionValue("VALUE_1");
        option.setDisplayText("Value 1");

        CustomFieldEntity customFieldEntity = new CustomFieldEntity();
        customFieldEntity.setId(UUID.randomUUID());
        customFieldEntity.setName("CUSTOM_FIELD_1");
        customFieldEntity.setType(getDropDownTypeEntity());
        customFieldEntity.setDataType(getStringDataTypeEntity());
        customFieldEntity.setDisplayText("Custom Field 1");
        customFieldEntity.setOptions(List.of(option));

        UserCustomFieldEntity userCustomFieldEntity = new UserCustomFieldEntity();
        userCustomFieldEntity.setId(UUID.randomUUID());
        userCustomFieldEntity.setCustomField(customFieldEntity);
        userCustomFieldEntity.setCustomFieldValueString("TEST1");

        return userCustomFieldEntity;
    }

    private CreateCustomFieldDTO getCreateCustomFieldDto(CreateCustomFieldDTO.TypeEnum type,
                                                         CreateCustomFieldDTO.DataTypeEnum dataType) {
        CreateCustomFieldDTO customField = new CreateCustomFieldDTO();
        customField.setName("CUSTOM_FIELD_1");
        customField.setDisplayText("Custom Field 1");
        customField.setType(type);
        customField.setDataType(dataType);
        if (type.compareTo(CreateCustomFieldDTO.TypeEnum.DROP_DOWN_LIST) == 0) {
            CreateOrUpdateCustomFieldOptionDTO option = new CreateOrUpdateCustomFieldOptionDTO();
            option.setOptionValue("VALUE_1");
            option.setDisplayText("Value 1");
            customField.setOptions(List.of(option));
        }
        return customField;
    }

    private UpdateCustomFieldDTO getUpdateCustomFieldDto() {
        UpdateCustomFieldDTO customField = new UpdateCustomFieldDTO();
        customField.setName("CUSTOM_FIELD_1");
        customField.setDisplayText("Custom Field 1");
        CreateOrUpdateCustomFieldOptionDTO option = new CreateOrUpdateCustomFieldOptionDTO();
        option.setOptionValue("VALUE_1");
        option.setDisplayText("Value 1");
        customField.setOptions(List.of(option));
        return customField;
    }

    private CustomFieldTypeEntity getDropDownTypeEntity() {
        CustomFieldTypeEntity customFieldTypeEntity = new CustomFieldTypeEntity();
        customFieldTypeEntity.setId(UUID.fromString("a80a48b5-2995-4c54-9bd5-ebc258fab4ba"));
        customFieldTypeEntity.setType("drop_down_list");
        return customFieldTypeEntity;
    }

    private CustomFieldTypeEntity getTextFieldTypeEntity() {
        CustomFieldTypeEntity customFieldTypeEntity = new CustomFieldTypeEntity();
        customFieldTypeEntity.setId(UUID.fromString("5fc38fee-e8f5-11ec-8fea-0242ac120002"));
        customFieldTypeEntity.setType("text_field");
        return customFieldTypeEntity;
    }

    private CustomFieldDataTypeEntity getStringDataTypeEntity() {
        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("3e724ddf-4d09-452b-ae98-a8e3a76af19c"));
        customFieldDataTypeEntity.setType("string");
        return customFieldDataTypeEntity;
    }

    private CustomFieldDataTypeEntity getIntDataTypeEntity() {
        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("7c6e4de3-5461-4a38-bdf8-2f853c50e3a3"));
        customFieldDataTypeEntity.setType("int");
        return customFieldDataTypeEntity;
    }

    private CustomFieldDataTypeEntity getJsonDataTypeEntity() {
        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("69af3a92-b6d6-4b6a-90e5-51304cba887c"));
        customFieldDataTypeEntity.setType("json");
        return customFieldDataTypeEntity;
    }

    private CustomFieldDataTypeEntity getDateTimeDataTypeEntity() {
        CustomFieldDataTypeEntity customFieldDataTypeEntity = new CustomFieldDataTypeEntity();
        customFieldDataTypeEntity.setId(UUID.fromString("5d01d9e3-f8a8-42e3-877c-b743bff79e7f"));
        customFieldDataTypeEntity.setType("datetime");
        return customFieldDataTypeEntity;
    }

    private CustomFieldEntity getCustomFieldEntity() {
        UserCustomFieldEntity userCustomFieldEntity = getUserCustomFieldEntity();
        return userCustomFieldEntity.getCustomField();
    }

    private CreateOrUpdateCustomFieldOptionDTO getCreateOrUpdateCustomFieldOptionDto() {
        CreateOrUpdateCustomFieldOptionDTO option = new CreateOrUpdateCustomFieldOptionDTO();
        option.setOptionValue("VALUE_1");
        option.setDisplayText("Value 1");
        return option;
    }
}
