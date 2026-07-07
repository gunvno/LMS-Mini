package vn.com.atomi.charge.quiz.model.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.quiz.model.enums.QuizAttemptStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class QuizAttemptDto extends BaseDto<String> {
    private String quizId;

    private String userId;

    private String enrollmentId;

    private BigDecimal score;

    private Boolean passed;

    private LocalDateTime startedAt;

    private LocalDateTime submittedAt;

    private QuizAttemptStatus status;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Valid
    private List<QuizAttemptAnswerInputDto> answers = new ArrayList<>();
}
