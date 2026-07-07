package vn.com.atomi.charge.quiz.model.dto;

import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.quiz.model.enums.QuizAttemptStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
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

    @Enumerated(EnumType.STRING)
    private QuizAttemptStatus status;
}
