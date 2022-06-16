package io.nuvalence.user.management.api.service.config;

import org.springframework.stereotype.Component;


/**
 * Class to run after startup.
 */
@Component
public class RunAfterStartUp {
    /* This comment can be removed if firebase auth is set up
    @Value("${firebase.credential.resource-path}")
    private String keyPath;

    /**
     * Initializes Firebase on server start with the application ready event listener.
     * @throws IOException if errors.
     */
    /* This comment can be removed if firebase auth is set up
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
    } */
}
