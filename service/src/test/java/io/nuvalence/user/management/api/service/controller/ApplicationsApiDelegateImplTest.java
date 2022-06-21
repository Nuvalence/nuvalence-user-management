package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.models.ApplicationDTO;
import io.nuvalence.user.management.api.service.service.ApplicationService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class ApplicationsApiDelegateImplTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ApplicationService applicationService;

    @Test
    @WithMockUser
    public void getApplications() throws Exception {
        List<ApplicationDTO> applicationList = createApplications();

        ResponseEntity<List<ApplicationDTO>> res = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(applicationList);

        when(applicationService.getApplications()).thenReturn(res);

        mockMvc.perform(get("/api/v2/applications"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getApplicationById() throws Exception {
        ApplicationDTO application = createApplication("TEST_1", "Test 1");

        ResponseEntity<ApplicationDTO> res = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(application);

        when(applicationService.getApplicationById(any())).thenReturn(res);

        mockMvc.perform(get("/api/v2/applications/" + application.getId().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(String.valueOf(application.getId())))
                .andExpect(jsonPath("$.name").value(application.getName()))
                .andExpect(jsonPath("$.displayName").value(application.getDisplayName()));
    }

    private ApplicationDTO createApplication(String name, String displayName) {
        ApplicationDTO app = new ApplicationDTO();
        app.setId(UUID.randomUUID());
        app.setName(name);
        app.setDisplayName(displayName);
        return app;
    }

    private List<ApplicationDTO> createApplications() {
        ApplicationDTO app0 = createApplication("group_a", "Group A");
        ApplicationDTO app1 = createApplication("group_b", "Group B");
        ApplicationDTO app2 = createApplication("group_c", "Group C");

        return List.of(app0, app1, app2);
    }
}