package vn.com.atomi.charge.quiz.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_quiz_attempt_answers")
public class QuizAttemptAnswerEntity extends BaseEntity {

    @Column(name = "attempt_id", nullable = false)
    private String attemptId;

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "answer_id")
    private String answerId;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "score")
    private BigDecimal score;

}
