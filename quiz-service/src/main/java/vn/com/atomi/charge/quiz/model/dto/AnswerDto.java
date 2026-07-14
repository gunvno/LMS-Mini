package vn.com.atomi.charge.quiz.model.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;

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

    @NotNull(groups = Create.class)
    @Schema(description = "correct of answer", example = "true")
    private Boolean correct;

    @Schema(description = "Automatically assigned by the server", example = "1", accessMode = Schema.AccessMode.READ_ONLY)
    private Integer orderIndex;
}
