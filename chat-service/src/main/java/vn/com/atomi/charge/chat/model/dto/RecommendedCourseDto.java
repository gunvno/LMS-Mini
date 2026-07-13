package vn.com.atomi.charge.chat.model.dto;

import java.math.BigDecimal;

public record RecommendedCourseDto(
        String id,
        String name,
        String description,
        String level,
        Integer durationMinutes,
        BigDecimal price
) {
}
