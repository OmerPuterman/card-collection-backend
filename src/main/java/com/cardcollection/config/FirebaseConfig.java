package com.cardcollection.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import jakarta.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Configuration
public class FirebaseConfig {

    @PostConstruct
    public void initialize() {
        try {
            String firebaseCredentials = System.getenv("FIREBASE_CREDENTIALS");

            InputStream serviceAccount;
            if (firebaseCredentials != null && !firebaseCredentials.isEmpty()) {
                // Use environment variable (for cloud deployment)
                System.out.println("✅ Using Firebase credentials from environment variable");
                serviceAccount = new ByteArrayInputStream(firebaseCredentials.getBytes(StandardCharsets.UTF_8));
            } else {
                // Use file (for local development)
                System.out.println("✅ Using Firebase credentials from file");
                ClassPathResource resource = new ClassPathResource("firebase-service-account.json");
                serviceAccount = new FileInputStream(resource.getFile());
            }

            FirebaseOptions options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .build();

            FirebaseApp.initializeApp(options);
            System.out.println("✅ Firebase initialized successfully");
        } catch (Exception e) {
            System.err.println("❌ Firebase initialization failed: " + e.getMessage());
            throw new RuntimeException("Failed to initialize Firebase", e);
        }
    }

    @Bean
    public Firestore firestore() {
        return FirestoreClient.getFirestore();
    }
}