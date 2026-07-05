package vn.com.atomi.charge.base.model.request;

import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class BaseRequest<T> {

    @Valid
    private T data;

    private String channel;

    private String signature;
}
