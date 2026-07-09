package vn.com.atomi.charge.notice.config.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import jakarta.annotation.PostConstruct;
import java.io.FileInputStream;

@Configuration
public class FirebaseConfig {

    @Value("${config.firebase.enabled:false}")
    private boolean firebaseEnabled;

    @Value("${config.firebase.credentials-path:}")
    private String credentialsPath;

    @PostConstruct
    public void initFirebase() throws Exception {
        if (!firebaseEnabled) {
            return;
        }

        if (!FirebaseApp.getApps().isEmpty()) {
            return;
        }

        FirebaseOptions.Builder builder = FirebaseOptions.builder();

        if (StringUtils.hasText(credentialsPath)) {
            try (FileInputStream serviceAccount = new FileInputStream(credentialsPath)) {
                builder.setCredentials(GoogleCredentials.fromStream(serviceAccount));
            }
        } else {
            builder.setCredentials(GoogleCredentials.getApplicationDefault());
        }

        FirebaseApp.initializeApp(builder.build());
    }
}
