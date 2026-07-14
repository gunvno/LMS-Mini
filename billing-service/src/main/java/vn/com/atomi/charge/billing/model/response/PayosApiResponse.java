package vn.com.atomi.charge.billing.model.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayosApiResponse<T> {
    private String code;
    private String desc;
    private T data;
}
