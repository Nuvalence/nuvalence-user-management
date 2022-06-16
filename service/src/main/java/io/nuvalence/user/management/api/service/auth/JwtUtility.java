package io.nuvalence.user.management.api.service.auth;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import io.nuvalence.user.management.api.service.config.exception.TokenAuthorizationException;
import io.nuvalence.user.management.api.service.generated.models.TokenRefreshPacket;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom JWT utility class to handle token operations.
 */
@Component
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:ClassDataAbstractionCoupling"})
public class JwtUtility {

    private static final Logger log = LoggerFactory.getLogger(JwtUtility.class);
    private final String apiKey;

    @Autowired
    public JwtUtility(@Value("${firebase.credential.api-key}") String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Creates a new auth token from refresh token.
     * @param refreshToken refresh token as string.
     * @return token refresh packet.
     * @throws IOException if there is an issue with streaming http response.
     */
    public TokenRefreshPacket createAuthTokenPacket(String refreshToken) throws IOException {

        String path = "https://securetoken.googleapis.com/v1/token?key=" + apiKey;

        final CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httppost = new HttpPost(path);

        // Request parameters and other properties.
        List<NameValuePair> params = new ArrayList<>(2);
        params.add(new BasicNameValuePair("grant_type", "refresh_token"));
        params.add(new BasicNameValuePair("refresh_token", refreshToken));
        httppost.setEntity(new UrlEncodedFormEntity(params, "UTF-8"));

        //Execute and get the response.
        HttpResponse response = httpclient.execute(httppost);
        HttpEntity entity = response.getEntity();


        if (entity != null) {
            StringBuilder sb = new StringBuilder();
            try (Reader in = new BufferedReader(new InputStreamReader(entity.getContent()))) {
                for (int c; (c = in.read()) >= 0;) {
                    sb.append((char) c);
                }
                String postResponse = sb.toString();
                JSONObject tokenResponse = new JSONObject(postResponse);
                String idToken = tokenResponse.getString("id_token");
                String newRefreshToken = tokenResponse.getString("refresh_token");
                TokenRefreshPacket tokenRefreshPacket = new TokenRefreshPacket();
                tokenRefreshPacket.setRefreshToken(newRefreshToken);
                tokenRefreshPacket.setAccessToken(idToken);
                return tokenRefreshPacket;
            }
        } else {
            throw new TokenAuthorizationException("No response received from token and api key.");
        }
    }

    /**
     * Verifies access token and gives security token.
     * @param token jwt token as string.
     * @return UsernameAndPassword Token for security context.
     * @throws FirebaseAuthException if firebase is not authenticated.
     */
    public static UsernameAuthenticationToken verifyAccessTokenForSecurityContext(String token)
            throws FirebaseAuthException {

        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(token);
            List<SimpleGrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
            return new UsernameAuthenticationToken(authorities, decodedToken.getUid(), decodedToken.getUid());
        } catch (FirebaseAuthException authException) {
            log.error("Error in token verification: {}", authException.getMessage());
            throw authException;
        }
    }
}