package io.nuvalence.user.management.api.service.service;

import io.nuvalence.user.management.api.service.auth.JwtUtility;
import io.nuvalence.user.management.api.service.config.exception.TokenAuthorizationException;
import io.nuvalence.user.management.api.service.generated.models.TokenRefreshPacket;
import io.nuvalence.user.management.api.service.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import javax.servlet.http.HttpServletRequest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private JwtUtility jwtUtility;

    @InjectMocks
    private AuthService authService;

    @Test
    public void refreshToken_returnsATokenRefreshPacket() throws IOException {
        // these aren't actual tokens, I spammed my keyboard.
        String bearerToken = "Bearer u43thup3q23ov3sdgqhq.ncn409t730sdynyqvoyov.47y8v4yiutoqy34";
        String refreshToken = "u43thup3q23ov3sdgqhq.ncn409t730sdynyqvoyov.47y8v4yiutoqy34";
        String newAccessToken = "436nf34yn3yn42vunintu382tn8vy23nt7vy209y4tvn923yn0.4nv39v67y23954yu2t3yv5t7890v2y3nt"
                + "890vy23tv72y3809yv0238y4tv23yv4982ynu3895tyvn238yvn83yu4tv892y3489t2v3984yv293ytiu34cyn3ytuy34toy23"
                + "40t7y.unyeroty237vy627vty3to2y35tv798y325tv789y5097v0y89ty2cv9034ytn7f3y4t79ycvn327894ty23n908y5ct"
                + "n73yt34nyctyn7834yctn034ytcy34c2347ytcn34ycto723y4nto7cvy237ot9ync23794tyc274ty3274ctyn237y4ncvt732"
                + "5ynt72340y78";
        TokenRefreshPacket refreshPacket = new TokenRefreshPacket();
        refreshPacket.setRefreshToken(refreshToken);
        refreshPacket.setAccessToken(newAccessToken);

        when(request.getHeader(AUTHORIZATION)).thenReturn(bearerToken);
        when(jwtUtility.createAuthTokenPacket(refreshToken)).thenReturn(refreshPacket);

        ResponseEntity<TokenRefreshPacket> res = authService.refreshToken(request);
        assertEquals(res.getStatusCode(), HttpStatus.OK);
        assertEquals(res.getBody().getAccessToken(), newAccessToken);
        assertEquals(res.getBody().getRefreshToken(), refreshToken);

    }

    @Test
    public void refreshToken_throwsExceptionWithNoAuthHeader() {

        Exception exception = assertThrows(TokenAuthorizationException.class, () -> {
            authService.refreshToken(request);
        });
        assertEquals(exception.getMessage(), "Incorrect authorization parameters.");
    }

    @Test
    public void refreshToken_throwsExceptionIfTheresAnInvalidToken() {

        String bearerToken = "Bearer u43thup3q23ov3sdgqhq.ncn409t730sdynyqvoyov.47y8v4yiutoqy34";
        when(request.getHeader(AUTHORIZATION)).thenReturn(bearerToken);

        Exception exception = assertThrows(TokenAuthorizationException.class, () -> {
            authService.refreshToken(request);
        });
        assertEquals(exception.getMessage(), "The provided refresh token is invalid");
    }
}