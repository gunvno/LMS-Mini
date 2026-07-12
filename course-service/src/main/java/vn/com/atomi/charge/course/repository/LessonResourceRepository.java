package vn.com.atomi.charge.course.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.course.model.entity.LessonResourceEntity;
import vn.com.atomi.charge.course.model.enums.LessonResourceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LessonResourceRepository extends BaseRepository<LessonResourceEntity, String> {

    Page<LessonResourceEntity> findByLessonIdAndStatusAndDeletedAtIsNull(
            String lessonId, LessonResourceStatus status, Pageable pageable);

    @Query("""
            select r from LessonResourceEntity r, LessonEntity l, CourseEntity c
            where r.lessonId = l.id
              and l.courseId = c.id
              and c.instructorId = :instructorId
              and r.deletedAt is null
              and l.deletedAt is null
              and c.deletedAt is null
            """)
    Page<LessonResourceEntity> findByInstructorId(
            @Param("instructorId") String instructorId, Pageable pageable);
}
