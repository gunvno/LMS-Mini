package vn.com.atomi.charge.course.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.course.model.enums.LessonResourceStatus;
import vn.com.atomi.charge.course.model.enums.LessonResourceType;

@Getter
@Setter
@Schema(description = "Lesson resource data")
public class LessonResourceDto extends BaseDto<String> {

    @NotBlank(groups = Create.class)
    @Schema(example = "lesson-id")
    private String lessonId;

    @NotBlank(groups = Create.class)
    @Schema(example = "Slide deck")
    private String title;

    @NotNull(groups = Create.class)
    @Schema(example = "PDF", allowableValues = {"PDF", "DOCX", "LINK", "VIDEO", "IMAGE"})
    private LessonResourceType resourceType;

    @Schema(example = "resources/java-backend/intro.pdf")
    private String filePath;

    @Schema(example = "https://example.com/resource")
    private String externalUrl;

    @NotNull(groups = Create.class)
    @Schema(example = "ACTIVE", allowableValues = {"ACTIVE", "INACTIVE"})
    private LessonResourceStatus status;
}
