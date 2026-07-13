package vn.com.atomi.charge.course.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.enums.CourseStatus;

import java.util.Optional;
import java.util.List;

public interface CourseRepository extends BaseRepository<CourseEntity, String> {

    boolean existsByCodeAndDeletedAtIsNull(String code);

    boolean existsByCodeAndIdNotAndDeletedAtIsNull(String code, String id);

    boolean existsByIdAndStatusAndDeletedAtIsNull(String id, CourseStatus status);

    Optional<CourseEntity> findByIdAndStatusAndDeletedAtIsNull(String id, CourseStatus status);

    Page<CourseEntity> findByStatusAndDeletedAtIsNull(CourseStatus status, Pageable pageable);

    Page<CourseEntity> findByStatusAndCategoryIdAndDeletedAtIsNull(
            CourseStatus status,
            String categoryId,
            Pageable pageable
    );

    Page<CourseEntity> findByInstructorIdAndDeletedAtIsNull(String instructorId, Pageable pageable);

    List<CourseEntity> findByInstructorIdAndDeletedAtIsNull(String instructorId);

    List<CourseEntity> findByStatusAndDeletedAtIsNullOrderByPublishedAtDesc(
            CourseStatus status, Pageable pageable);
}
