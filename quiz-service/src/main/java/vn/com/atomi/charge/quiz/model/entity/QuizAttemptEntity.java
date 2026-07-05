package vn.com.atomi.charge.quiz.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizAttemptStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_quiz_attempts")
public class QuizAttemptEntity extends BaseEntity {

    @Column(name = "quiz_id", nullable = false)
    private String quizId;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "enrollment_id", nullable = false)
    private String enrollmentId;

    @Column(name = "score")
    private BigDecimal score;

    @Column(name = "passed")
    private Boolean passed;

    @Column(name = "started_at", nullable = false)
    private LocalDateTime startedAt;

    @Column(name = "submitted_at")
    private LocalDateTime submittedAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuizAttemptStatus status;

}
