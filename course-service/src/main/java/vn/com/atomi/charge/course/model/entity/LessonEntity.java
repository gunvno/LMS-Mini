package vn.com.atomi.charge.course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.course.model.enums.LessonStatus;

@Getter
@Setter
@Entity
@Table(name = "tbl_lessons")
public class LessonEntity extends BaseEntity {

    @Column(name = "course_id", nullable = false)
    private String courseId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "code")
    private String code;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "video_url", length = 1000)
    private String videoUrl;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "duration_minutes")
    private Integer durationMinutes;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LessonStatus status;

}
