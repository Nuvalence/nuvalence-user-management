package io.nuvalence.user.management.api.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.entity.ApplicationEntity;
import io.nuvalence.user.management.api.service.entity.PermissionEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdatePermissionDTO;
import io.nuvalence.user.management.api.service.generated.models.PermissionDTO;
import io.nuvalence.user.management.api.service.mapper.PermissionEntityMapper;
import io.nuvalence.user.management.api.service.repository.PermissionRepository;
import io.nuvalence.user.management.api.service.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class PermissionApiDelegateImplTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private PermissionRepository permissionRepository;

    @MockBean
    private PermissionService permissionService;

    @Test
    @WithMockUser
    public void addPermission() throws Exception {
        CreateOrUpdatePermissionDTO permission = createOrUpdatePermissionModel();
        ResponseEntity<Void> res = ResponseEntity.ok().build();
        when(permissionService.addPermission(permission)).thenReturn(res);
        final String postBody = new ObjectMapper().writeValueAsString(permission);

        mockMvc.perform(
                        post("/api/v2/permission")
                                .content(postBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void updatePermission() throws Exception {
        PermissionEntity permission = createMockPermission();

        CreateOrUpdatePermissionDTO permissionModel = createOrUpdatePermissionModel();
        ResponseEntity<Void> res = ResponseEntity.ok().build();
        when(permissionService.updatePermission(any(), any())).thenReturn(res);
        final String putBody = new ObjectMapper().writeValueAsString(permissionModel);

        mockMvc.perform(
                        put("/api/v2/permission/" + permission.getId().toString())
                                .content(putBody)
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getAllPermissions() throws Exception {
        PermissionEntity permissionEntity = createMockPermission();
        PermissionDTO permissionModel = PermissionEntityMapper.INSTANCE
                .permissionEntityToPermissionDto(permissionEntity);
        ResponseEntity<List<PermissionDTO>> res = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(List.of(permissionModel));
        when(permissionService.getAllPermissions()).thenReturn(res);

        mockMvc.perform(get("/api/v2/permission")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(String.valueOf(permissionEntity.getId())))
                .andExpect(jsonPath("$[0].displayName").value(permissionEntity.getDisplayName()))
                .andExpect(jsonPath("$[0].description").value(permissionEntity.getDescription()))
                .andExpect(jsonPath("$[0].name").value(permissionEntity.getName()));
    }

    @Test
    @WithMockUser
    public void deletePermissionById() throws Exception {
        PermissionEntity permissionEntity = createMockPermission();
        when(permissionRepository.findById(any())).thenReturn(Optional.of(permissionEntity));

        mockMvc.perform(delete("/api/v2/permission/" + UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getPermissionById() throws Exception {
        PermissionEntity permissionEntity = createMockPermission();
        when(permissionRepository.findById(permissionEntity.getId())).thenReturn(Optional.of(permissionEntity));

        mockMvc.perform(get(
                "/api/v2/permission/" + permissionEntity.getId().toString())
        ).andExpect(status().isOk());
    }

    private CreateOrUpdatePermissionDTO createOrUpdatePermissionModel() {
        CreateOrUpdatePermissionDTO permission = new CreateOrUpdatePermissionDTO();
        permission.setName("test_perm");
        permission.setDisplayName("Test Permission");
        permission.setDescription("This is a test permission.");
        return permission;
    }

    private PermissionEntity createMockPermission() {
        PermissionEntity permission = new PermissionEntity();
        permission.setId(UUID.randomUUID());
        permission.setName("test_perm");
        permission.setDisplayName("Test Permission");
        permission.setDescription("This is a test permission.");
        return permission;
    }

    private List<ApplicationEntity> createMockApplicationList() {
        ApplicationEntity app1 = new ApplicationEntity();
        app1.setName("APPLICATION_1");
        app1.setId(UUID.randomUUID());
        ApplicationEntity app2 = new ApplicationEntity();
        app2.setName("APPLICATION_2");
        app2.setId(UUID.randomUUID());
        return List.of(app1, app2);
    }
}