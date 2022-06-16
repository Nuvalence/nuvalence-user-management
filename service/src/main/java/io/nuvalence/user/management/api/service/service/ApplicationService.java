package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.config.exception.ResourceNotFoundException;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.generated.models.ApplicationDTO;
import io.nuvalence.user.management.api.service.mapper.ApplicationEntityMapper;
import io.nuvalence.user.management.api.service.repository.ApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for retrieving applications.
 */
@Component
@RequiredArgsConstructor
public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    /**
     * Returns a list of all applications.
     * @return list of applications
     */
    public ResponseEntity<List<ApplicationDTO>> getApplications() {

        List<ApplicationEntity> applicationEntities = applicationRepository.findAll();

        List<ApplicationDTO> applications = applicationEntities.stream().map(a -> {
            ApplicationDTO application = ApplicationEntityMapper.INSTANCE.applicationEntityToApplicationDto(a);
            return application;
        }).collect(Collectors.toList());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(applications);
    }

    /**
     * Returns an application by its id.
     *
     * @param id the id of the application.
     * @return an application.
     */
    public ResponseEntity<ApplicationDTO> getApplicationById(UUID id) {
        Optional<ApplicationEntity> applicationEntity = applicationRepository.findById(id);
        if (applicationEntity.isEmpty()) {
            throw new ResourceNotFoundException("Application not found!");
        }

        ApplicationDTO application = ApplicationEntityMapper.INSTANCE
                .applicationEntityToApplicationDto(applicationEntity.get());

        return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(application);
    }
}