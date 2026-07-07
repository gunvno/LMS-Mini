package vn.com.atomi.charge.gateway.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseResponseDto<T> {
    private T data;

    private String status;

    private String errorCode;

    private String message;
}
