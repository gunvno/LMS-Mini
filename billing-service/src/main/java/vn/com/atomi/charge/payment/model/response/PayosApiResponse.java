package vn.com.atomi.charge.payment.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayosApiResponse<T> {
    private String code;
    private String desc;
    private T data;
}
