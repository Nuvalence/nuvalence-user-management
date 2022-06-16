package io.nuvalence.user.management.api.service.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Class to run after startup.
 */
@Component
public class RunAfterStartUp {

    @Value("${firebase.credential.resource-path}")
    private String keyPath;

    /**
     * Initializes Firebase on server start with the application ready event listener.
     * @throws IOException if errors.
     */
    @Bean
    @Primary
    @EventListener(ApplicationReadyEvent.class)
    public void firebaseInitialization() throws IOException {
        InputStream serviceAccount = new ByteArrayInputStream(keyPath.getBytes(StandardCharsets.UTF_8));
        FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();
        if (FirebaseApp.getApps().isEmpty()) {
            FirebaseApp.initializeApp(options);
        }
    }
}
