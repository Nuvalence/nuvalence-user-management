package io.nuvalence.user.management.api.service.auth;

import org.json.JSONException;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.slf4j.Logger;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
public class JwtUtilityTest {

    @Mock
    private Logger log;

    @InjectMocks
    private JwtUtility jwtUtility;

    @Test
    public void createAuthTokenPacket_ThrowsExceptionIfTokenInvalid() {
        String mockToken = "ThisIsAMockToken";

        Exception exception = assertThrows(JSONException.class, () -> {
            jwtUtility.createAuthTokenPacket(mockToken);
        });
        assertEquals(exception.getMessage(), "JSONObject[\"id_token\"] not found.");
    }

    /* This comment can be removed if firebase auth is set up
    @Test
    public void verifyAccessTokenForSecurityContext_ThrowsFireBaseExceptionIfTokenInvalid() {
        String mockToken = "ThisIsAMockToken";

        Exception exception = assertThrows(FirebaseAuthException.class, () -> {
            JwtUtility.verifyAccessTokenForSecurityContext(mockToken);
        });
        assertEquals(exception.getMessage(), "Failed to parse Firebase ID token. Make sure you passed a "
                + "string that represents a complete and valid JWT. See"
                + " https://firebase.google.com/docs/auth/admin/verify-id-tokens "
                + "for details on how to retrieve an ID token.");
    }
    */
}