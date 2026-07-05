package vn.com.atomi.charge.course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.course.model.enums.CourseLevel;
import vn.com.atomi.charge.course.model.enums.CourseStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "tbl_courses")
public class CourseEntity extends BaseEntity {

    @Column(name = "category_id", nullable = false)
    private String categoryId;

    @Column(name = "instructor_id", nullable = false)
    private String instructorId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "code", nullable = false)
    private String code;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "level")
    @Enumerated(EnumType.STRING)
    private CourseLevel level;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private CourseStatus status;

    @Column(name = "reject_reason", columnDefinition = "TEXT")
    private String rejectReason;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

}
