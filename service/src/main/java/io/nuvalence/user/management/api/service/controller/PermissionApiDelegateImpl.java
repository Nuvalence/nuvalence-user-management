package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.controllers.PermissionApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.service.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Controller for Role API.
 */
@Service
@RequiredArgsConstructor
public class PermissionApiDelegateImpl implements PermissionApiDelegate {
    private final PermissionService permissionService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return PermissionApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<Void> addPermission(CreateOrUpdatePermissionDTO body) {
        return permissionService.addPermission(body);
    }

    @Override
    public ResponseEntity<Void> updatePermission(UUID id, CreateOrUpdatePermissionDTO body) {
        return permissionService.updatePermission(id, body);
    }

    @Override
    public ResponseEntity<List<PermissionDTO>> getAllPermissions() {
        return permissionService.getAllPermissions();
    }

    @Override
    public ResponseEntity<Void> deletePermissionById(UUID id) {
        return permissionService.deletePermissionById(id);
    }

    @Override
    public ResponseEntity<PermissionDTO> getPermissionById(UUID id) {
        return permissionService.getPermissionById(id);
    }
}