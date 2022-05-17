package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationLanguageEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.repository.ApplicationLanguagesRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ApplicationLanguageServiceTest {

    private static List<ApplicationEntity> apps = null;
    private static List<LanguageEntity> langs = null;
    private static List<ApplicationLanguageEntity> applicationLanguageEntities = null;

    @Mock
    private ApplicationLanguagesRepository applicationLanguagesRepository;

    @InjectMocks
    private ApplicationLanguageService applicationLanguageService;

    @BeforeAll
    public static void setupLanguages() {
        apps = createApplications();
        langs = createLanguages();
        applicationLanguageEntities = createApplicationLanguages(langs, apps);
    }

    /**
     * Tests if the correct languages are retrieved with the corresponding ID. For this test,
     * a given application has multiple languages mapped to it.
     */
    @Test
    public void getSupportedLanguages_validId_Multiple() {
        List<ApplicationLanguageEntity> supportedLanguages = List.of(
                applicationLanguageEntities.get(0),
                applicationLanguageEntities.get(1),
                applicationLanguageEntities.get(2)
        );

        when(applicationLanguagesRepository.findLanguagesByApplicationId(apps.get(0).getId())).thenReturn(
                supportedLanguages
        );

        List<LanguageEntity> result = applicationLanguageService.getSupportedLanguagesById(apps.get(0).getId());

        List<LanguageEntity> expected = supportedLanguages
                .stream()
                .map(ApplicationLanguageEntity::getLanguage
        ).collect(Collectors.toList());

        assertEquals(result, expected);
    }

    /**
     * Test if the correct language is retrieved with the corresponding ID. For this test, a given application
     * has only 1 language mapped to it.
     */
    @Test
    public void getSupportedLanguages_validId_Single() {
        List<ApplicationLanguageEntity> supportedLanguages = List.of(applicationLanguageEntities.get(0));

        when(applicationLanguagesRepository.findLanguagesByApplicationId(apps.get(2).getId())).thenReturn(
                supportedLanguages
        );

        List<LanguageEntity> result = applicationLanguageService.getSupportedLanguagesById(apps.get(2).getId());

        List<LanguageEntity> expected = supportedLanguages
                .stream()
                .map(ApplicationLanguageEntity::getLanguage)
                .collect(Collectors.toList());

        assertEquals(result, expected);
    }

    /**
     * Test if an error is thrown if an invalid ID is passed.
     */
    @Test
    public void getSupportedLanguages_invalidId() {
        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            applicationLanguageService.getSupportedLanguagesById(UUID.randomUUID());
        });

        assertEquals(exception.getMessage(), "There are no applications with the given ID, or this "
                + "application has no supported languages.");
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

    private static ApplicationLanguageEntity createApplicationLanguage(LanguageEntity lang, ApplicationEntity app) {
        ApplicationLanguageEntity appLang = new ApplicationLanguageEntity();
        appLang.setLanguage(lang);
        appLang.setApplication(app);
        return appLang;
    }

    private static List<ApplicationLanguageEntity> createApplicationLanguages(List<LanguageEntity> langs,
                                                                              List<ApplicationEntity> apps) {
        /*
        * Setup relationships between the languages and applications.
        * */
        return List.of(
                createApplicationLanguage(langs.get(0), apps.get(0)),
                createApplicationLanguage(langs.get(1), apps.get(0)),
                createApplicationLanguage(langs.get(2), apps.get(0)),

                createApplicationLanguage(langs.get(0), apps.get(1)),
                createApplicationLanguage(langs.get(1), apps.get(1)),

                createApplicationLanguage(langs.get(0), apps.get(2))
        );
    }
}
