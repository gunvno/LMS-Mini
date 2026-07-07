package vn.com.atomi.charge.quiz.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class QuizAttemptAnswerInputDto {

    @NotBlank
    private String questionId;

    @NotBlank
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String answerId;
}