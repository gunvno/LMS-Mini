package vn.com.atomi.charge.quiz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.quiz.model.enums.QuestionType;

import java.math.BigDecimal;
@Getter
@Setter
@Schema(description = "Question resource data")
public class QuestionDto extends BaseDto<String> {
    @NotBlank(groups = Create.class)
    @Schema(description = "Quiz id of question", example = "quiz-001")
    private String quizId;

    @NotBlank(groups = Create.class)
    @Schema(description = "Content of question", example = "Java supports multiple inheritance through class?")
    private String content;

    @NotNull(groups = Create.class)
    @Schema(description = "Type of question", example = "SINGLE_CHOICE", allowableValues = {"SINGLE_CHOICE"})
    private QuestionType questionType;

    @NotNull(groups = Create.class)
    @Schema(description = "Score of question", example = "0.5")
    private BigDecimal score;

    @NotNull(groups = Create.class)
    @Schema(description = "Display order in quiz", example = "1")
    private Integer orderIndex;
}
