package io.nuvalence.user.management.api.service.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.nuvalence.user.management.api.service.entity.UserEntity;
import io.nuvalence.user.management.api.service.generated.models.UserCreationRequest;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import io.nuvalence.user.management.api.service.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public class CloudTaskApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private UserService userService;

    @Test
    @WithMockUser
    public void addUser() throws Exception {
        UserCreationRequest user = createNewUserModel();
        ResponseEntity<Void> res = ResponseEntity.status(200).build();
        when(userService.createUser(user)).thenReturn(res);
        final String postBody = new ObjectMapper().writeValueAsString(user);

        mockMvc.perform(
                post("/api/v2/cloud-task/user")
                        .content(postBody)
                        .contentType(MediaType.APPLICATION_JSON)
        ).andExpect(status().isOk());
    }

    @Test
    @WithMockUser
    public void deleteUserById() throws Exception {
        UserEntity userEntity = createMockUser();
        when(userRepository.findById(userEntity.getId())).thenReturn(Optional.of(userEntity));

        mockMvc.perform(delete("/api/v2/cloud-task/user/" + userEntity.getId().toString()))
                .andExpect(status().isOk());
    }

    private UserCreationRequest createNewUserModel() {
        UserCreationRequest user = new UserCreationRequest();
        user.setExternalId("SMGjTO5n3sZFVIi5IzpW2pI8vjf1");
        user.setDisplayName("Rusty Popins");
        user.setEmail("Rdawg@gmail.com");
        return user;
    }

    private UserEntity createMockUser() {
        UserEntity user = new UserEntity();
        user.setId(UUID.randomUUID());
        user.setDisplayName("Rusty Popins");
        user.setEmail("Rdawg@gmail.com");
        user.setExternalId("SMGjTO5n3sZFVIi5IzpW2pI8vjf1");
        return user;
    }
}