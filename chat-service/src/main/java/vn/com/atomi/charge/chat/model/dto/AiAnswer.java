package vn.com.atomi.charge.chat.model.dto;

import java.util.List;

public record AiAnswer(String content, List<RecommendedCourseDto> recommendedCourses) {
}
