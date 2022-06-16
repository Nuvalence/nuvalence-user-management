package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.controllers.ApplicationsApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.ApplicationDTO;
import io.nuvalence.user.management.api.service.service.ApplicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Routes for application(s).
 */
@Service
@RequiredArgsConstructor
public class ApplicationsApiDelegateImpl implements ApplicationsApiDelegate {
    private final ApplicationService applicationService;

    @Override
    public ResponseEntity<List<ApplicationDTO>> getApplications() {
        return applicationService.getApplications();
    }
}