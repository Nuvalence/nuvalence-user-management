package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationLanguageEntity;
import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.repository.ApplicationLanguagesRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import javax.transaction.Transactional;

/**
 * Service for retrieving languages.
 */
@Component
@Transactional
@RequiredArgsConstructor
public class ApplicationLanguageService {

    private final ApplicationLanguagesRepository applicationLanguagesRepository;

    /**
     * For a given application ID, return a list of supported language entities.
     * @param id Application ID.
     * @return List of language entities supported by the application.
     */
    public List<LanguageEntity> getSupportedLanguagesById(UUID id) {
        List<ApplicationLanguageEntity> supportedLanguages =
                applicationLanguagesRepository.findLanguagesByApplicationId(id);

        if (supportedLanguages.isEmpty()) {
            throw new ResourceNotFoundException("There are no applications with the given ID, or this application has"
                                                + " no supported languages.");
        }

        return supportedLanguages.stream().map(ApplicationLanguageEntity::getLanguage).collect(Collectors.toList());
    }
}
