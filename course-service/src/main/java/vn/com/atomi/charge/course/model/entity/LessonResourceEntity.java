package vn.com.atomi.charge.course.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import vn.com.atomi.charge.base.model.entity.BaseEntity;
import vn.com.atomi.charge.course.model.enums.LessonResourceStatus;
import vn.com.atomi.charge.course.model.enums.LessonResourceType;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tbl_lesson_resources")
public class LessonResourceEntity extends BaseEntity {

    @Column(name = "lesson_id", nullable = false)
    private String lessonId;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(name = "resource_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private LessonResourceType resourceType;

    @Column(name = "file_path", length = 1000)
    private String filePath;

    @Column(name = "external_url", length = 1000)
    private String externalUrl;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private LessonResourceStatus status;

}
