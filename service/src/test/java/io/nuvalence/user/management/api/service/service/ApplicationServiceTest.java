package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationDTO;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Tests for ApplicationService.
 */
@ExtendWith(MockitoExtension.class)
public class ApplicationServiceTest {

    private static List<ApplicationEntity> apps = null;

    @Mock
    private ApplicationRepository applicationRepository;

    @InjectMocks
    private ApplicationService applicationService;

    @BeforeAll
    public static void setupLanguages() {
        apps = createApplications();
    }

    /**
     * Tests if the application service returns a successful 2xx response with 3 applications.
     */
    @Test
    void getsAllApplications() {
        when(applicationRepository.findAll()).thenReturn(apps);

        ResponseEntity<List<ApplicationDTO>> response = applicationService.getApplications();

        assertEquals(3, response.getBody().stream().count());
        assertTrue(response.getStatusCode().is2xxSuccessful());
    }

    @Test
    void getApplicationById() {
        when(applicationRepository.findById(any())).thenReturn(Optional.of(createApplication("TEST_1")));

        ResponseEntity<ApplicationDTO> response = applicationService.getApplicationById(UUID.randomUUID());
        assertTrue(response.getStatusCode().is2xxSuccessful());
        assertTrue(Objects.requireNonNull(response.getBody()).getName().equalsIgnoreCase("TEST_1"));
    }

    @Test
    void getApplicationById_fails_ifApplicationDoesNotExist() {
        when(applicationRepository.findById(any())).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () ->
                applicationService.getApplicationById(UUID.randomUUID()));
        assertTrue(exception.getMessage().contains("Application not found!"));
    }

    private static LanguageEntity createLanguage(String name, String standardId) {
        LanguageEntity lang = new LanguageEntity();
        lang.setId(UUID.randomUUID());
        lang.setLanguageName(name);
        lang.setLanguageStandardId(standardId);
        return lang;
    }

    private static List<LanguageEntity> createLanguages() {
        LanguageEntity language0 = createLanguage("English", "en");
        LanguageEntity language1 = createLanguage("Spanish", "es");
        LanguageEntity language2 = createLanguage("Greek", "el");

        return List.of(language0, language1, language2);
    }

    private static ApplicationEntity createApplication(String name) {
        ApplicationEntity app = new ApplicationEntity();
        app.setId(UUID.randomUUID());
        app.setName(name);
        return app;
    }

    private static List<ApplicationEntity> createApplications() {
        ApplicationEntity app0 = createApplication("group_a");
        ApplicationEntity app1 = createApplication("group_b");
        ApplicationEntity app2 = createApplication("group_c");

        return List.of(app0, app1, app2);
    }
}