package vn.com.atomi.charge.chat.security;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConversationTokenServiceTest {

    private final ConversationTokenService service = new ConversationTokenService();

    @Test
    void tokenMatchesOnlyItsOwnHash() {
        String token = service.createToken();
        String anotherToken = service.createToken();

        assertThat(service.matches(token, service.hash(token))).isTrue();
        assertThat(service.matches(anotherToken, service.hash(token))).isFalse();
        assertThat(service.matches("", service.hash(token))).isFalse();
    }
}
