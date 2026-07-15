package vn.com.atomi.charge.notice.exception;

import com.google.firebase.messaging.MessagingErrorCode;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FirebasePushExceptionTest {

    @Test
    void onlyUnregisteredErrorIsTerminalForInstallation() {
        assertThat(exception(MessagingErrorCode.INVALID_ARGUMENT).isInvalidInstallation()).isFalse();
        assertThat(exception(MessagingErrorCode.UNREGISTERED).isInvalidInstallation()).isTrue();
        assertThat(exception(MessagingErrorCode.UNAVAILABLE).isInvalidInstallation()).isFalse();
    }

    private FirebasePushException exception(MessagingErrorCode code) {
        return new FirebasePushException(code, code.name(), null);
    }
}
