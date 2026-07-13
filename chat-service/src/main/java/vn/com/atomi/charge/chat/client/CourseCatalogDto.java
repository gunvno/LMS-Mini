package vn.com.atomi.charge.chat.client;

import java.math.BigDecimal;

public record CourseCatalogDto(
        String id,
        String name,
        String description,
        String level,
        Integer durationMinutes,
        BigDecimal price
) {
}
