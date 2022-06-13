package io.nuvalence.user.management.api.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.generated.models.CreateCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateCustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.CustomFieldOptionDTO;
import io.nuvalence.user.management.api.service.generated.models.UpdateCustomFieldDTO;
import io.nuvalence.user.management.api.service.service.CustomFieldService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class CustomFieldsApiDelegateImplTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomFieldService customFieldService;

    @Test
    public void getAllCustomFields() throws Exception {
        List<CustomFieldDTO> fields = List.of(getMockCustomFieldDto());
        ResponseEntity<List<CustomFieldDTO>> res = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(fields);
        when(customFieldService.getAllCustomFields()).thenReturn(res);

        mockMvc.perform(
                get("/api/v2/custom-fields"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(fields.get(0).getName()));
    }

    @Test
    public void addCustomField() throws Exception {
        ResponseEntity<Void> res = ResponseEntity.ok().build();
        CreateCustomFieldDTO customField = getMockCreateCustomFieldDto();
        when(customFieldService.addCustomField(getMockCreateCustomFieldDto()))
                .thenReturn(res);
        final String postBody = new ObjectMapper().writeValueAsString(customField);

        mockMvc.perform(
                post("/api/v2/custom-fields")
                        .content(postBody)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void getCustomFieldById() throws Exception {
        CustomFieldDTO customField = getMockCustomFieldDto();
        ResponseEntity<CustomFieldDTO> res = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(customField);
        when(customFieldService.getCustomFieldById(any())).thenReturn(res);

        mockMvc.perform(
                get("/api/v2/custom-fields/" + customField.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(customField.getName()));
    }

    @Test
    public void updateCustomField() throws Exception {
        UpdateCustomFieldDTO customField = getMockUpdateCustomFieldDto();
        ResponseEntity<Void> res = ResponseEntity.ok().build();
        when(customFieldService.updateCustomField(any(), any())).thenReturn(res);
        final String putBody = new ObjectMapper().writeValueAsString(customField);

        mockMvc.perform(
                put("/api/v2/custom-fields/" + UUID.randomUUID())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putBody))
                .andExpect(status().isOk());
    }

    @Test
    public void deleteCustomField() throws Exception {
        ResponseEntity<Void> res = ResponseEntity.ok().build();
        when(customFieldService.deleteCustomField(any())).thenReturn(res);

        mockMvc.perform(
                delete("/api/v2/custom-fields/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    public void getOptionsForCustomField() throws Exception {
        List<CustomFieldOptionDTO> options = List.of(getMockCustomFieldOptionDto());
        ResponseEntity<List<CustomFieldOptionDTO>> res = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(options);
        when(customFieldService.getOptionsForCustomField(any())).thenReturn(res);

        mockMvc.perform(
                get("/api/v2/custom-fields/" + UUID.randomUUID() + "/options"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].optionValue").value(options.get(0).getOptionValue()));
    }

    @Test
    public void updateCustomFieldOptions() throws Exception {
        List<CreateOrUpdateCustomFieldOptionDTO> options = List.of(getMockCreateOrUpdateCustomFieldOptionDto());
        ResponseEntity<Void> res = ResponseEntity.ok().build();
        final String putBody = new ObjectMapper().writeValueAsString(options);
        when(customFieldService.updateCustomFieldOptions(any(), any())).thenReturn(res);

        mockMvc.perform(
                put("/api/v2/custom-fields/" + UUID.randomUUID() + "/options")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(putBody))
                .andExpect(status().isOk());
    }

    private CustomFieldDTO getMockCustomFieldDto() {
        CustomFieldDTO customField = new CustomFieldDTO();
        customField.setId(UUID.randomUUID());
        customField.setName("TEST_FIELD_1");
        customField.setDisplayText("Test Field 1");
        customField.setType(CustomFieldDTO.TypeEnum.TEXT_FIELD);
        customField.setDataType(CustomFieldDTO.DataTypeEnum.STRING);
        return customField;
    }

    private CreateCustomFieldDTO getMockCreateCustomFieldDto() {
        CreateCustomFieldDTO customField = new CreateCustomFieldDTO();
        customField.setName("TEST_FIELD_1");
        customField.setDisplayText("Test Field 1");
        customField.setDataType(CreateCustomFieldDTO.DataTypeEnum.STRING);
        customField.setType(CreateCustomFieldDTO.TypeEnum.TEXT_FIELD);
        return customField;
    }

    private UpdateCustomFieldDTO getMockUpdateCustomFieldDto() {
        UpdateCustomFieldDTO customField = new UpdateCustomFieldDTO();
        customField.setName("TEST_FIELD_1");
        customField.setDisplayText("Test Field 1");
        return customField;
    }

    private CustomFieldOptionDTO getMockCustomFieldOptionDto() {
        CustomFieldOptionDTO option = new CustomFieldOptionDTO();
        option.setId(UUID.randomUUID());
        option.setOptionValue("OPTION_1");
        option.setDisplayText("Option 1");
        return option;
    }

    private CreateOrUpdateCustomFieldOptionDTO getMockCreateOrUpdateCustomFieldOptionDto() {
        CreateOrUpdateCustomFieldOptionDTO option = new CreateOrUpdateCustomFieldOptionDTO();
        option.setOptionValue("OPTION_1");
        option.setDisplayText("Option 1");
        return option;
    }
}
