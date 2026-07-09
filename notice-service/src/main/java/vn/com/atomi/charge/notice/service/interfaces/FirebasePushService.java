package vn.com.atomi.charge.notice.service.interfaces;

import java.util.Map;

public interface FirebasePushService {

    String send(String fcmToken, String title, String body, Map<String, String> data);
}