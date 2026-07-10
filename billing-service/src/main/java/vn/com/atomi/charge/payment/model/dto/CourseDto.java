package vn.com.atomi.charge.payment.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CourseDto {
    private String id;
    private String name;
    private BigDecimal price;
}
