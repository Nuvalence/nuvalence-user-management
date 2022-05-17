package io.nuvalence.user.management.api.service.mapper;

import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.generated.models.LanguageDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
public class LanguageEntityMapperTest {

    /**
     * Tests if Language entities are properly mapped to Language DTOs.
     */
    @Test
    public void shouldMapLanguageEntityToLanguageDto() {
        LanguageEntity language = new LanguageEntity();
        language.setId(UUID.randomUUID());
        language.setLanguageName("English");
        language.setLanguageStandardId("en");

        LanguageDTO languageDto = LanguageEntityMapper.INSTANCE.languageEntityToLanguageDto(language);

        assertEquals(language.getId(), languageDto.getId());
        assertEquals(language.getLanguageName(), languageDto.getLanguageName());
        assertEquals(language.getLanguageStandardId(), languageDto.getLanguageStandardId());
    }

}
