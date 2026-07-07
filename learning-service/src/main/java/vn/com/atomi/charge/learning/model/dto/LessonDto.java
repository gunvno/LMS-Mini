package vn.com.atomi.charge.learning.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.learning.model.enums.LessonStatus;

@Getter
@Setter
@Schema(description = "Lesson data")
public class LessonDto extends BaseDto<String> {

    @NotBlank(groups = Create.class)
    @Schema(example = "course-id")
    private String courseId;

    @NotBlank(groups = Create.class)
    @Schema(example = "Introduction")
    private String title;

    @Schema(example = "LESSON_001")
    private String code;

    private String content;

    @Schema(example = "https://example.com/video.mp4")
    private String videoUrl;

    @NotNull(groups = Create.class)
    @Schema(example = "1")
    private Integer orderIndex;

    @Schema(example = "30")
    private Integer durationMinutes;

    @NotNull(groups = Create.class)
    @Schema(example = "ACTIVE", allowableValues = {"DRAFT", "ACTIVE", "INACTIVE", "ARCHIVED"})
    private LessonStatus status;
}
