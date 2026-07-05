package vn.com.atomi.charge.base.model.response;

import lombok.*;
import org.springframework.http.HttpStatus;
import vn.com.atomi.charge.base.model.enums.BaseErrorCode;

@Data
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class BaseResponse<T> {

    private T data;

    private HttpStatus status;

    private String errorCode = BaseErrorCode.SUCCESS.getErrorCode();

    private String message;

    public static <T> BaseResponse<T> success(HttpStatus status, T data) {
      return new BaseResponse<>(data, HttpStatus.OK, BaseErrorCode.SUCCESS.getErrorCode(), status.name());
    }

    public static <T> BaseResponse<T> fail(HttpStatus status, String message) {
      return new BaseResponse<>(null, status, BaseErrorCode.FAILURE.getErrorCode(), message);
    }
}
