package io.nuvalence.user.management.api.service.auth;

import io.nuvalence.user.management.api.service.generated.models.TokenRefreshPacket;
import io.nuvalence.user.management.api.service.service.AuthService;
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

import javax.servlet.http.HttpServletRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class AuthApiDelegateImplTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;


    @Test
    @WithMockUser
    public void refreshToken_returnsATokenPacket() throws Exception {
        TokenRefreshPacket refreshPacket = new TokenRefreshPacket();
        refreshPacket.setRefreshToken("ThisIsAJwt");
        refreshPacket.setAccessToken("ThisIsARealDeal-AlyMcBealToken");
        ResponseEntity<TokenRefreshPacket> res = ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_JSON).body(refreshPacket);

        when(authService.refreshToken(any(HttpServletRequest.class))).thenReturn(res);

        mockMvc.perform(get("/api/v2/auth/token/refresh").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refreshToken").value("ThisIsAJwt"))
                .andExpect(jsonPath("$.accessToken").value("ThisIsARealDeal-AlyMcBealToken"));
    }

}