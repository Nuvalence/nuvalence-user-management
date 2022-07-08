package io.nuvalence.user.management.api.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.service.UserPreferenceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Abstracts out the User Preference specific API endpoints for testing.
 */

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@WithMockUser
public class UserPreferenceApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserPreferenceService userPreferenceService;

    @Test
    public void getUserPreferences() throws Exception {
        UserEntity user = createMockUser();
        UserPreferenceDTO preferences = createUserPreferenceDto(user.getId(), null);

        when(userPreferenceService.getUserPreferences(user.getId(), null)).thenReturn(preferences);

        mockMvc.perform(get("/api/v2/user/" + user.getId().toString() + "/preferences")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("communication").value("phone"))
                .andExpect(jsonPath("language").value("el"));
    }

    @Test
    public void getUserApplicationPreferences() throws Exception {
        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());
        application.setName("my-awesome-application");

        UserEntity user = createMockUser();

        UserPreferenceDTO preferences = createUserPreferenceDto(user.getId(), application.getId());

        when(userPreferenceService.getUserPreferences(user.getId(), application.getId()))
                .thenReturn(preferences);

        mockMvc.perform(
                        get("/api/v2/user/" + user.getId().toString() + "/preferences/" + application.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("communication").value("phone"))
                .andExpect(jsonPath("language").value("el"));
    }

    @Test
    public void updateUserPreferences() throws Exception {
        UserEntity user = createMockUser();
        UserPreferenceDTO preferences = createUserPreferenceDto(user.getId(), null);
        doNothing().when(userPreferenceService).updateUserPreferences(user.getId(), preferences);
        final String preferencesBody = new ObjectMapper().writeValueAsString(preferences);

        mockMvc.perform(
                put("/api/v2/user/" + user.getId().toString() + "/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(preferencesBody)
        ).andExpect(status().isOk());
    }

    @Test
    public void updateApplicationPreferences() throws Exception {
        UserEntity user = createMockUser();
        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());
        application.setName("my-awesome-application");
        UserPreferenceDTO preferences = createUserPreferenceDto(user.getId(), application.getId());
        doNothing().when(userPreferenceService)
                .updateUserApplicationPreferences(user.getId(), application.getId(), preferences);
        final String preferencesBody = new ObjectMapper().writeValueAsString(preferences);

        mockMvc.perform(
                put("/api/v2/user/" + user.getId().toString() + "/preferences")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(preferencesBody)
        ).andExpect(status().isOk());
    }

    private UserEntity createMockUser() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setDisplayName("Rusty Popins");
        user.setEmail("Rdawg@gmail.com");
        return user;
    }

    private UserPreferenceDTO createUserPreferenceDto(UUID userId, UUID applicationId) {
        UserPreferenceDTO preferences = new UserPreferenceDTO();
        preferences.setUserId(userId);
        preferences.setApplicationId(applicationId);
        preferences.put("communication", "phone");
        preferences.put("language", "el");
        return preferences;
    }
}
