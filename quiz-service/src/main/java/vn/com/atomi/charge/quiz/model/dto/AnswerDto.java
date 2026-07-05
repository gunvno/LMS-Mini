package vn.com.atomi.charge.quiz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;

@Getter
@Setter
@Schema(description = "Answer resource data")

public class AnswerDto extends BaseDto<String> {
    @NotBlank(groups = Create.class)
    @Schema(description = "question of answer", example = "question")
    private String questionId;

    @NotBlank(groups = Create.class)
    @Schema(description = "content of answer", example = "Learn ApiRestful")
    private String content;

    @NotBlank(groups = Create.class)
    @Schema(description = "correct of answer", example = "true")
    private Boolean correct;

    @NotBlank(groups = Create.class)
    @Schema(description = "where answer is putted in", example = "1")
    private Integer orderIndex;
}
