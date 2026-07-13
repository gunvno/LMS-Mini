package vn.com.atomi.charge.chat.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import vn.com.atomi.charge.base.model.response.BaseResponse;

@RestControllerAdvice(basePackages = "vn.com.atomi.charge.chat")
public class ChatExceptionHandler {

    @ExceptionHandler(ChatException.class)
    public ResponseEntity<BaseResponse<Void>> handle(ChatException exception) {
        return ResponseEntity.status(exception.getStatus())
                .body(BaseResponse.fail(exception.getStatus(), exception.getMessage()));
    }
}
