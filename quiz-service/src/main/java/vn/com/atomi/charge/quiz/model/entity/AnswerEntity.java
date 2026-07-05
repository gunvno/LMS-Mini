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
@Table(name = "tbl_answers")
public class AnswerEntity extends BaseEntity {

    @Column(name = "question_id", nullable = false)
    private String questionId;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_correct")
    private Boolean correct;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

}
