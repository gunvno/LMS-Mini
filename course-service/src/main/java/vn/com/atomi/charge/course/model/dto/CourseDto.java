package vn.com.atomi.charge.course.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.course.model.enums.CourseLevel;
import vn.com.atomi.charge.course.model.enums.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Schema(description = "Course data")
public class CourseDto extends BaseDto<String> {

    @NotBlank(groups = Create.class)
    @Schema(example = "c4c9e832-3a32-46d1-b8b8-efcf04f2f31b")
    private String categoryId;

    @NotBlank(groups = Create.class)
    @Schema(example = "instructor-001")
    private String instructorId;

    @NotBlank(groups = Create.class)
    @Schema(example = "Java Backend Foundation")
    private String name;

    @NotBlank(groups = Create.class)
    @Schema(example = "JAVA_BACKEND_001")
    private String code;

    @Schema(example = "Foundation course for Java backend development")
    private String description;

    @Schema(example = "BEGINNER", allowableValues = {"BEGINNER", "INTERMEDIATE", "ADVANCED"})
    private CourseLevel level;

    @Schema(example = "120")
    private Integer durationMinutes;

    @Schema(example = "199000")
    private BigDecimal price;

    @NotNull(groups = Create.class)
    @Schema(example = "DRAFT", allowableValues = {"DRAFT", "INSTRUCTOR_DRAFT", "PENDING_REVIEW", "PUBLISHED", "REJECTED", "ARCHIVED"})
    private CourseStatus status;

    private String rejectReason;

    private LocalDateTime publishedAt;
}
