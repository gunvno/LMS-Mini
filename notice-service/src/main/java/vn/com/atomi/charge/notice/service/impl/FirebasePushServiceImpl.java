package vn.com.atomi.charge.notice.service.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.notice.service.interfaces.FirebasePushService;

import java.util.Map;

@Service
public class FirebasePushServiceImpl implements FirebasePushService {

    @Override
    public String send(String fcmToken, String title, String body, Map<String, String> data) {
        if (!StringUtils.hasText(fcmToken)) {
            throw new IllegalArgumentException("FCM token is required");
        }

        Message.Builder builder = Message.builder()
                .setToken(fcmToken)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());

        if (data != null && !data.isEmpty()) {
            builder.putAllData(data);
        }

        try {
            return FirebaseMessaging.getInstance().send(builder.build());
        } catch (Exception ex) {
            throw new IllegalStateException(ex.getMessage(), ex);
        }
    }
}