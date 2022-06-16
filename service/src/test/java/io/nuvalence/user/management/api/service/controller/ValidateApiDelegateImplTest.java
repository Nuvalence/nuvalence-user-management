package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.models.ValidatePermissionDTO;
import io.nuvalence.user.management.api.service.service.ValidateService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ValidateApiDelegateImplTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ValidateService validateService;

    @Test
    @WithMockUser
    public void validateUserRole() throws Exception {
        ValidatePermissionDTO validateRole = new ValidatePermissionDTO();
        validateRole.setAllow(true);

        ResponseEntity<ValidatePermissionDTO> res = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                        .body(validateRole);
        when(validateService.validateUserPermission(
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString())).thenReturn(res);

        mockMvc.perform(
                get("/api/v2/validate/user-permission?resource=default_resource")
                        .param("userName", "allow")
                        .param("permission", "permissionToTest")
                        .param("resource", "default_resource")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.allow").value(validateRole.getAllow()));
    }
}
