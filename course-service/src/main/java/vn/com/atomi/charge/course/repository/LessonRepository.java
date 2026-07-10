package vn.com.atomi.charge.course.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.course.model.entity.LessonEntity;

import java.util.List;

public interface LessonRepository extends BaseRepository<LessonEntity, String> {

    boolean existsByCourseIdAndCodeAndDeletedAtIsNull(String courseId, String code);

    boolean existsByCourseIdAndCodeAndIdNotAndDeletedAtIsNull(String courseId, String code, String id);

    Page<LessonEntity> findByCourseIdAndDeletedAtIsNull(String courseId, Pageable pageable);

    @Query("""
            select l from LessonEntity l, CourseEntity c
            where l.courseId = c.id
              and c.instructorId = :instructorId
              and l.deletedAt is null
              and c.deletedAt is null
            """)
    Page<LessonEntity> findByInstructorId(@Param("instructorId") String instructorId, Pageable pageable);

    List<LessonEntity> findByCourseIdAndDeletedAtIsNullOrderByOrderIndexAscCreatedDateAsc(
            String courseId
    );
    long countByCourseIdAndDeletedAtIsNull(String courseId);

    @Query("""
            select coalesce(sum(coalesce(l.durationMinutes, 0)), 0)
            from LessonEntity l
            where l.courseId = :courseId and l.deletedAt is null
            """)
    Integer sumDurationMinutesByCourseId(@Param("courseId") String courseId);
}
