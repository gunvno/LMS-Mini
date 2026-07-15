package vn.com.atomi.charge.notice.service.interfaces;

import java.util.Map;

public interface FirebasePushService {

    String send(String installationId, String title, String body, Map<String, String> data);
}
