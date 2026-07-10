package vn.com.atomi.charge.payment.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayosApiResponse<T> {
    private String code;
    private String desc;
    private T data;
}
