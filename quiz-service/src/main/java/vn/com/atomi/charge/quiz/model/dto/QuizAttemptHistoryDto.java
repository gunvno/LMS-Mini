package vn.com.atomi.charge.quiz.model.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.quiz.model.enums.QuizAttemptStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
public class QuizAttemptHistoryDto {
    private String id;
    private Integer attemptNumber;
    private BigDecimal score;
    private Boolean passed;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime startedAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime submittedAt;
    private QuizAttemptStatus status;
}
