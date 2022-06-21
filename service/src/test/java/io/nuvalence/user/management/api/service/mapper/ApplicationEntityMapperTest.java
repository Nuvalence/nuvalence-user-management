package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.ApplicationLanguageEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class ApplicationEntityMapperTest {

    /**
     * Tests if Application entities are properly mapped to Application DTOs.
     */
    @Test
    void shouldMapApplicationEntityToApplicationDto() {
        ApplicationEntity applicationEntity = new ApplicationEntity();
        applicationEntity.setId(UUID.randomUUID());
        applicationEntity.setName("APPLICATION_NAME");
        applicationEntity.setDisplayName("Application Name");

        LanguageEntity language = new LanguageEntity();
        language.setId(UUID.randomUUID());
        language.setLanguageName("English");
        language.setLanguageStandardId("en");

        ApplicationLanguageEntity applicationLanguageEntity = new ApplicationLanguageEntity();
        applicationLanguageEntity.setId(UUID.randomUUID());
        applicationLanguageEntity.setApplication(applicationEntity);
        applicationLanguageEntity.setLanguage(language);

        List<ApplicationLanguageEntity> supportedLanguages = new ArrayList<>();
        supportedLanguages.add(applicationLanguageEntity);

        applicationEntity.setSupportedLanguages(supportedLanguages);

        ApplicationDTO applicationDto = ApplicationEntityMapper.INSTANCE
                .applicationEntityToApplicationDto(applicationEntity);
        assertEquals(applicationEntity.getId(), applicationDto.getId());
        assertEquals(applicationEntity.getName(), applicationDto.getName());
        assertEquals(applicationEntity.getDisplayName(), applicationDto.getDisplayName());
        assertEquals(applicationEntity.getSupportedLanguages().get(0).getLanguage().getId(),
                applicationDto.getSupportedLanguages().get(0).getId());
        assertEquals(applicationEntity.getSupportedLanguages().get(0).getLanguage().getLanguageName(),
                applicationDto.getSupportedLanguages().get(0).getLanguageName());
    }
}