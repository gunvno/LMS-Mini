package vn.com.atomi.charge.quiz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;

import java.math.BigDecimal;

@Getter
@Setter
@Schema(description = "Lesson resource data")
public class QuizDto extends BaseDto<String> {
    @NotBlank(groups = Create.class)
    @Schema(description = "Course of quiz", example = "Backend")
    private String courseId;

    @NotBlank(groups = Create.class)
    @Schema(description = "Lesson of quiz", example = "Java")
    private String lessonId;

    @NotBlank(groups = Create.class)
    @Schema(description = "Title of quiz", example = "Learn ApiRestful")
    private String title;

    @NotNull(groups = Create.class)
    @Schema(description = "PassScore of quiz", example = "70")
    private BigDecimal passScore;

    @NotNull(groups = Create.class)
    @Schema(description = "MaxAttempts of quiz", example = "6")
    private Integer maxAttempts;

    @NotNull(groups = Create.class)
    @Schema(description = "requiredToComplete of quiz", example = "true")
    private Boolean requiredToComplete;

    @NotNull(groups = Create.class)
    @Schema(description = "status of quiz", example = "ACTIVE")
    @Enumerated(EnumType.STRING)
    private QuizStatus status;
}
