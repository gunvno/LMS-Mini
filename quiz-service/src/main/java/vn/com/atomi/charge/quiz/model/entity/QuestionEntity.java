package vn.com.atomi.charge.quiz.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.quiz.model.enums.QuestionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_questions")
public class QuestionEntity extends BaseEntity {

    @Column(name = "quiz_id", nullable = false)
    private String quizId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "question_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "score", nullable = false)
    private BigDecimal score;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

}
