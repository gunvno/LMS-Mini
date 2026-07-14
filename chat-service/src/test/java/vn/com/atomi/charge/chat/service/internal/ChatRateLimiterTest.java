package vn.com.atomi.charge.chat.service.internal;

import org.junit.jupiter.api.Test;

import java.time.Clock;

import static org.assertj.core.api.Assertions.assertThat;

class ChatRateLimiterTest {

    @Test
    void rejectsRequestsAfterConfiguredLimit() {
        ChatRateLimiter limiter = new ChatRateLimiter(2, Clock.systemUTC());

        assertThat(limiter.allow("127.0.0.1")).isTrue();
        assertThat(limiter.allow("127.0.0.1")).isTrue();
        assertThat(limiter.allow("127.0.0.1")).isFalse();
        assertThat(limiter.allow("127.0.0.2")).isTrue();
    }
}
