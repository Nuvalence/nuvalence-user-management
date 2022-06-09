package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.controllers.CustomFieldsApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.CreateCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateCustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.UpdateCustomFieldDTO;
import io.nuvalence.user.management.api.service.service.CustomFieldService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for Custom Field API.
 */

@Service
@RequiredArgsConstructor
public class CustomFieldApiDelegateImpl implements CustomFieldsApiDelegate {
    private final CustomFieldService customFieldService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return CustomFieldsApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<List<CustomFieldDTO>> getAllCustomFields() {
        return customFieldService.getAllCustomFields();
    }

    @Override
    public ResponseEntity<Void> addCustomField(CreateCustomFieldDTO customField) {
        return customFieldService.addCustomField(customField);
    }

    @Override
    public ResponseEntity<CustomFieldDTO> getCustomFieldById(UUID id) {
        return customFieldService.getCustomFieldById(id);
    }

    @Override
    public ResponseEntity<Void> updateCustomField(UUID id, UpdateCustomFieldDTO customField) {
        return customFieldService.updateCustomField(id, customField);
    }

    @Override
    public ResponseEntity<Void> deleteCustomField(UUID id) {
        return customFieldService.deleteCustomField(id);
    }

    @Override
    public ResponseEntity<List<CustomFieldOptionDTO>> getOptionsForCustomField(UUID id) {
        return customFieldService.getOptionsForCustomField(id);
    }

    @Override
    public ResponseEntity<Void> updateCustomFieldOptions(UUID id, List<CreateOrUpdateCustomFieldOptionDTO> options) {
        return customFieldService.updateCustomFieldOptions(id, options);
    }
}
