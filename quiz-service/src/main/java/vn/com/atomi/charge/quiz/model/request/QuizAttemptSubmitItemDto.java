package vn.com.atomi.charge.quiz.model.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Schema(description = "Submitted answer data for a single question")
public class QuizAttemptSubmitItemDto {

    @NotBlank
    @Schema(description = "Question id", example = "uuid")
    private String questionId;

    @Schema(description = "Selected answer ids", example = "[\"uuid-1\", \"uuid-2\"]")
    private List<String> answerIds = new ArrayList<>();
}