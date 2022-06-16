package io.nuvalence.user.management.api.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.entity.UserPreferenceEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationPreferenceDTO;
import io.nuvalence.user.management.api.service.generated.models.LanguageDTO;
import io.nuvalence.user.management.api.service.generated.models.UserPreferenceDTO;
import io.nuvalence.user.management.api.service.mapper.UserPreferenceEntityMapper;
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
public class UserPreferencesApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserPreferenceService userPreferenceService;

    @Test
    public void getPreferencesFromUser() throws Exception {
        UserEntity user = createMockUser();
        UserPreferenceEntity preferences = createMockUserPreferences();
        LanguageEntity language = preferences.getLanguage();

        when(userPreferenceService.getPreferencesByUserId(user.getId())).thenReturn(preferences);

        mockMvc.perform(get("/api/v2/user/" + user.getId().toString() + "/preferences")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("communicationPreference").value("sms"))
                .andExpect(jsonPath("language.id").value(language.getId().toString()))
                .andExpect(jsonPath("language.languageName").value(language.getLanguageName()))
                .andExpect(jsonPath("language.languageStandardId").value(language.getLanguageStandardId()));
    }

    @Test
    public void getSupportedPreferencesFromUser() throws Exception {
        UserPreferenceEntity preferences = createMockUserPreferences();

        LanguageEntity backupLanguage = createMockLanguage("Greek", "el");

        ApplicationEntity application = new ApplicationEntity();
        application.setId(UUID.randomUUID());
        application.setName("my-awesome-application");

        preferences.setLanguage(backupLanguage);

        UserEntity user = createMockUser();
        when(userPreferenceService.getSupportedPreferencesByUserId(user.getId(), application.getId()))
                .thenReturn(preferences);

        mockMvc.perform(
                        get("/api/v2/user/" + user.getId().toString() + "/preferences/" + application.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("communicationPreference").value("sms"))
                .andExpect(jsonPath("language.id").value(backupLanguage.getId().toString()))
                .andExpect(jsonPath("language.languageName").value(backupLanguage.getLanguageName()))
                .andExpect(jsonPath("language.languageStandardId")
                        .value(backupLanguage.getLanguageStandardId()));
    }

    @Test
    public void updateUserPreferences() throws Exception {
        UserEntity user = createMockUser();
        UserPreferenceEntity preferences = createMockUserPreferences();

        UserPreferenceDTO preferencesDto =
                UserPreferenceEntityMapper.INSTANCE.userPreferencesEntityToDto(preferences);

        doNothing().when(userPreferenceService).updatePreferencesByUserId(user.getId(), preferencesDto);

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

        ApplicationPreferenceDTO applicationPreferences = new ApplicationPreferenceDTO()
                .language(new LanguageDTO()
                        .id(UUID.randomUUID())
                        .languageName("Greek")
                        .languageStandardId("el"));

        doNothing().when(userPreferenceService)
                .updateApplicationPreferencesById(user.getId(), application.getId(), applicationPreferences);

        final String preferencesBody = new ObjectMapper().writeValueAsString(applicationPreferences);

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

    private UserPreferenceEntity createMockUserPreferences() {
        LanguageEntity language = createMockLanguage("English", "en");

        UserPreferenceEntity preference = new UserPreferenceEntity();
        preference.setId(UUID.randomUUID());
        preference.setCommunicationPreference("sms");
        preference.setLanguage(language);

        return preference;
    }

    private LanguageEntity createMockLanguage(String name, String standardId) {
        LanguageEntity language = new LanguageEntity();
        language.setId(UUID.randomUUID());
        language.setLanguageName(name);
        language.setLanguageStandardId(standardId);
        return language;
    }
}
