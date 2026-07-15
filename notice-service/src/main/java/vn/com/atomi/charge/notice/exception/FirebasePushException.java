package vn.com.atomi.charge.notice.exception;

import com.google.firebase.messaging.MessagingErrorCode;
import lombok.Getter;

@Getter
public class FirebasePushException extends RuntimeException {

    private final MessagingErrorCode messagingErrorCode;

    public FirebasePushException(MessagingErrorCode messagingErrorCode, String message, Throwable cause) {
        super(message, cause);
        this.messagingErrorCode = messagingErrorCode;
    }

    public boolean isInvalidInstallation() {
        // INVALID_ARGUMENT can also describe a malformed payload, so only the
        // terminal, installation-specific UNREGISTERED result is safe to retire.
        return messagingErrorCode == MessagingErrorCode.UNREGISTERED;
    }
}
