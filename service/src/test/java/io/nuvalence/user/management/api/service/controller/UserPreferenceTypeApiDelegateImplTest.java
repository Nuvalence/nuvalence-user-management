package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.models.UserPreferenceTypeDTO;
import io.nuvalence.user.management.api.service.service.UserPreferenceTypeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Abstracts out hte User Preference type specific API endpoints for testing.
 */
@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
public class UserPreferenceTypeApiDelegateImplTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserPreferenceTypeService userPreferenceTypeService;

    @Test
    public void getUserPreferenceTypes() throws Exception {
        ResponseEntity<List<UserPreferenceTypeDTO>> res = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(List.of(createUserPreferenceTypeDto()));
        when(userPreferenceTypeService.getAllUserPreferenceTypes()).thenReturn(res);
        mockMvc.perform(get("/api/v2/userPreferenceType")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    private UserPreferenceTypeDTO createUserPreferenceTypeDto() {
        UserPreferenceTypeDTO userPreferenceTypeEntity = new UserPreferenceTypeDTO();
        userPreferenceTypeEntity.setName("communication");
        userPreferenceTypeEntity.setId(UUID.randomUUID());
        userPreferenceTypeEntity.setOptions(List.of("email", "phone"));

        return userPreferenceTypeEntity;
    }
}
