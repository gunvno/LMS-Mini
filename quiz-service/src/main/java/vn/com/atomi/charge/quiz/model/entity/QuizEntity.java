package vn.com.atomi.charge.quiz.model.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_quizzes")
public class QuizEntity extends BaseEntity {

    @Column(name = "course_id")
    private String courseId;

    @Column(name = "lesson_id")
    private String lessonId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "pass_score", nullable = false)
    private BigDecimal passScore;

    @Column(name = "max_attempts")
    private Integer maxAttempts;

    @Column(name = "required_to_complete")
    private Boolean requiredToComplete;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuizStatus status;

}
