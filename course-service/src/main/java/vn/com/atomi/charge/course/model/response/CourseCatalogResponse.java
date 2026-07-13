package vn.com.atomi.charge.course.model.response;

import vn.com.atomi.charge.course.model.enums.CourseLevel;

import java.math.BigDecimal;

public record CourseCatalogResponse(
        String id,
        String name,
        String description,
        CourseLevel level,
        Integer durationMinutes,
        BigDecimal price
) {
}
