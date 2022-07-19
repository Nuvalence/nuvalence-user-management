package io.nuvalence.user.management.api.service.controller;

import io.nuvalence.user.management.api.service.generated.controllers.RoleApiDelegate;
import io.nuvalence.user.management.api.service.generated.models.RoleCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleUpdateRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.service.RoleService;
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
public class RoleApiDelegateImpl implements RoleApiDelegate {

    private final RoleService roleService;

    @Override
    public Optional<NativeWebRequest> getRequest() {
        return RoleApiDelegate.super.getRequest();
    }

    @Override
    public ResponseEntity<Void> addRole(RoleCreationRequest body) {
        return roleService.addRole(body);
    }

    @Override
    public ResponseEntity<Void> updateRole(UUID id, RoleUpdateRequest role) {
        return roleService.updateRole(id, role);
    }

    @Override
    public ResponseEntity<List<RoleDTO>> getAllRoles(String resource) {
        return roleService.getAllRolesByResource(resource);
    }

    @Override
    public ResponseEntity<Void> deleteRoleById(UUID id, String resource) {
        return roleService.deleteRoleById(id, resource);
    }

    @Override
    public ResponseEntity<List<UserDTO>> getUsersByRole(UUID id) {
        return roleService.getUsersByRoleId(id);
    }
}
