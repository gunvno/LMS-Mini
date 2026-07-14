package vn.com.atomi.charge.chat.model.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public class ChatException extends RuntimeException {
    private final HttpStatus status;

    public ChatException(HttpStatus status, String message) {
        super(message);
        this.status = status;
    }
}
