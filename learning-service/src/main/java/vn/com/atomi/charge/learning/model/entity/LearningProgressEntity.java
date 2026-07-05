package vn.com.atomi.charge.learning.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.learning.model.enums.LearningProgressStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_learning_progress")
public class LearningProgressEntity extends BaseEntity {

    @Column(name = "enrollment_id", nullable = false)
    private String enrollmentId;

    @Column(name = "lesson_id", nullable = false)
    private String lessonId;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LearningProgressStatus status;

}
