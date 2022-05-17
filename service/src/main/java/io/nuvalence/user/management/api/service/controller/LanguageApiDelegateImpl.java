package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.entity.LanguageEntity;
import io.nuvalence.user.management.api.service.generated.controllers.LanguagesApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.LanguageDTO;
import io.nuvalence.user.management.api.service.mapper.LanguageEntityMapper;
import io.nuvalence.user.management.api.service.service.ApplicationLanguageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Routes for languages supported by a given application(s).
 */
@Service
@RequiredArgsConstructor
public class LanguageApiDelegateImpl implements LanguagesApiDelegate {

    private final ApplicationLanguageService applicationLanguageService;

    @Override
    public ResponseEntity<List<LanguageDTO>> getSupportedLanguages(UUID id) {
        List<LanguageEntity> supportedLanguages = applicationLanguageService.getSupportedLanguagesById(id);

        /*
         * Since each application language is simply an application corresponding to a language, map each application
         * language to just the language object.
         */
        List<LanguageDTO> mappedLanguages = supportedLanguages
                .stream()
                .map(LanguageEntityMapper.INSTANCE::languageEntityToLanguageDto).collect(Collectors.toList());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(mappedLanguages);
    }
}
