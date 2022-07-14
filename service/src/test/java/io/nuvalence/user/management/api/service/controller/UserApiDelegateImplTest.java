package io.nuvalence.user.management.api.service.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.entity.RoleEntity;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.CreateOrUpdateUserCustomFieldDTO;
import io.nuvalence.user.management.api.service.generated.models.RoleDTO;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.generated.models.UserDTO;
import io.nuvalence.user.management.api.service.generated.models.UserRoleDTO;
import io.nuvalence.user.management.api.service.mapper.MapperUtils;
import io.nuvalence.user.management.api.service.mapper.RoleEntityMapper;
import io.nuvalence.user.management.api.service.mapper.UserEntityMapper;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import io.nuvalence.user.management.api.service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class UserApiDelegateImplTest {
    @Autowired
    private MockMvc mockMvc;

    @Mock
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    public void getUserById() throws Exception {
        UserEntity userEntity = createMockUser();
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));

        mockMvc.perform(get(
                "/api/v2/user/" + userEntity.getId().toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getUserByEmail() throws Exception {
        UserEntity userEntity = createMockUser();
        when(userRepository.findUserEntityByEmail(userEntity.getEmail())).thenReturn(Optional.of(userEntity));

        mockMvc.perform(get(
                        "/api/v2/user/email/"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void getUserList() throws Exception {
        UserEntity userEntity = createMockUser();
        UserDTO userModel = UserEntityMapper.INSTANCE
                .convertUserEntityToUserModel(userEntity);
        ResponseEntity<List<UserDTO>> res = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(List.of(userModel));
        when(userService.getUserList()).thenReturn(res);

        mockMvc.perform(get("/api/v2/user")
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id")
                        .value(String.valueOf(userEntity.getId())))
                .andExpect(jsonPath("$[0].displayName")
                        .value(userEntity.getDisplayName()));
    }

    @Test
    @WithMockUser
    public void getUserRolesById() throws Exception {
        UUID userId = createMockUser().getId();
        List<RoleDTO> roles = MapperUtils
                .mapRoleEntitiesToRoleList(createMockRoleList());
        ResponseEntity<List<RoleDTO>> res = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(roles);

        String expectedRoleName = roles.get(0).getRoleName();
        String expectedRoleName1 = roles.get(1).getRoleName();

        when(userService.getUserRolesById(eq(userId), ArgumentMatchers.anyString())).thenReturn(res);
        mockMvc.perform(get("/api/v2/user/" + userId.toString() + "/all-roles?resource=default_resource")
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roleName").value(expectedRoleName))
                .andExpect(jsonPath("$[1].roleName").value(expectedRoleName1));
    }

    @Test
    @WithMockUser
    public void assignRoleToUser() throws Exception {
        UserRoleDTO userRole = createMockUserRoleDto();

        ResponseEntity<Void> res = ResponseEntity.status(200).build();
        when(userService.assignRoleToUser(userRole)).thenReturn(res);
        final String postBody = new ObjectMapper().writeValueAsString(userRole);

        mockMvc.perform(
                post("/api/v2/user/adjust-roles")
                        .content(postBody)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void removeRoleFromUser() throws Exception {
        UserRoleDTO userRole = createMockUserRoleDto();

        ResponseEntity<Void> res = ResponseEntity.status(200).build();
        when(userService.removeRoleFromUser(userRole)).thenReturn(res);
        final String postBody = new ObjectMapper().writeValueAsString(userRole);

        mockMvc.perform(
                delete("/api/v2/user/adjust-roles")
                        .content(postBody)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void updateUserCustomFieldValue() throws Exception {
        CreateOrUpdateUserCustomFieldDTO customField = createMockUserCustomFieldDto();
        ResponseEntity<Void> res = ResponseEntity.ok().build();
        when(userService.updateCustomField(any(), any())).thenReturn(res);
        final String putBody = new ObjectMapper().writeValueAsString(customField);

        mockMvc.perform(
                put("/api/v2/user/" + UUID.randomUUID() + "/custom-field")
                        .content(putBody)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    private UserDTO createUserModel() {
        UserDTO user = new UserDTO();
        user.setId(UUID.randomUUID());
        user.setDisplayName("Rusty Popins");
        user.setEmail("Rdawg@gmail.com");
        return user;
    }

    private UserCreationRequest createNewUserModel() {
        UserCreationRequest user = new UserCreationRequest();
        user.setExternalId("TestExternalId1234");
        user.setDisplayName("Rusty Popins");
        user.setEmail("Rdawg@gmail.com");
        return user;
    }

    private UserEntity createMockUser() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setDisplayName("Rusty Popins");
        user.setEmail("Rdawg@gmail.com");
        return user;
    }

    private List<RoleEntity> createMockRoleList() {
        RoleEntity role = new RoleEntity();
        role.setRoleName("ROLE_TO_TEST_1");
        role.setId(UUID.randomUUID());
        RoleEntity role1 = new RoleEntity();
        role1.setRoleName("ROLE_TO_TEST_2");
        role1.setId(UUID.randomUUID());
        return List.of(role, role1);
    }

    private UserRoleDTO createMockUserRoleDto() {
        RoleDTO role = RoleEntityMapper.INSTANCE
                .roleEntityToRoleDto(createMockRoleList().get(0));
        UserDTO user = createUserModel();
        UserRoleDTO userRole = new UserRoleDTO();
        userRole.setRoleId(role.getId());
        userRole.setUserId(user.getId());
        return userRole;
    }

    private CreateOrUpdateUserCustomFieldDTO createMockUserCustomFieldDto() {
        CreateOrUpdateUserCustomFieldDTO customField = new CreateOrUpdateUserCustomFieldDTO();
        customField.setCustomFieldId(UUID.randomUUID());
        customField.setValue("OPTION_1");
        return customField;
    }

}
