package vn.com.atomi.charge.chat.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Component
public class ChatRateLimiter {

    private static final long WINDOW_MILLIS = 60_000L;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();
    private final int limit;
    private final Clock clock;

    @Autowired
    public ChatRateLimiter(@Value("${config.chatbot.rate-limit-per-minute:20}") int limit) {
        this(limit, Clock.systemUTC());
    }

    ChatRateLimiter(int limit, Clock clock) {
        this.limit = Math.max(1, limit);
        this.clock = clock;
    }

    public boolean allow(String clientKey) {
        long now = clock.millis();
        Window window = windows.compute(clientKey, (key, current) -> {
            if (current == null || now - current.startedAt() >= WINDOW_MILLIS) {
                return new Window(now, new AtomicInteger(1));
            }
            current.count().incrementAndGet();
            return current;
        });
        if (windows.size() > 10_000) {
            windows.entrySet().removeIf(entry -> now - entry.getValue().startedAt() >= WINDOW_MILLIS);
        }
        return window.count().get() <= limit;
    }

    private record Window(long startedAt, AtomicInteger count) {
    }
}
