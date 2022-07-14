package io.nuvalence.user.management.api.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.generated.models.RoleCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.service.RoleService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class RoleApiDelegateImplTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RoleService roleService;

    @Test
    @WithMockUser
    public void addRole() throws Exception {
        RoleCreationRequest roleCreationRequest = new RoleCreationRequest();

        ResponseEntity<Void> res = ResponseEntity.status(200).build();
        when(roleService.addRole(roleCreationRequest)).thenReturn(res);
        final String postBody = new ObjectMapper().writeValueAsString(roleCreationRequest);

        mockMvc.perform(
            post("/api/v2/role")
                .content(postBody)
                .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getAllRoles() throws Exception {
        List<RoleDTO> roles = List.of(createMockRoleDto());

        ResponseEntity<List<RoleDTO>> res = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(roles);
        when(roleService.getAllRolesByResource(ArgumentMatchers.anyString())).thenReturn(res);

        mockMvc.perform(
                get("/api/v2/role?resource=default_resource")
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$[0].roleName").value(roles.get(0).getRoleName()))
                        .andExpect(jsonPath("$[0].displayName").value(roles.get(0).getDisplayName()));
    }

    @Test
    @WithMockUser
    public void deleteRoleById() throws Exception {
        RoleDTO role = createMockRoleDto();

        ResponseEntity<Void> res = ResponseEntity.ok().build();
        when(roleService.deleteRoleById(eq(role.getId()), ArgumentMatchers.anyString())).thenReturn(res);

        mockMvc.perform(
                delete("/api/v2/role/" + role.getId().toString() + "?resource=default_resource")
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getUsersByRole() throws Exception {
        UserDTO user = new UserDTO();
        user.setEmail("ThatGuySmoke@yahoo.com");
        user.setDisplayName("Smokey Robinson");
        user.setId(UUID.randomUUID());

        RoleDTO role = createMockRoleDto();
        ResponseEntity<List<UserDTO>> res = ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                .body(List.of(user));
        when(roleService.getUsersByRoleId(eq(role.getId()))).thenReturn(res);

        mockMvc.perform(
                        get("/api/v2/role/" + role.getId().toString())
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].displayName").value(user.getDisplayName()));
    }

    private RoleDTO createMockRoleDto() {
        RoleDTO role = new RoleDTO();
        role.setRoleName("ROLE_TO_TEST");
        role.setDisplayName("Role To Test");
        role.setId(UUID.randomUUID());
        return role;
    }
}
