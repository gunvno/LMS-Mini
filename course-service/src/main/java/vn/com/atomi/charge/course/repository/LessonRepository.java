package vn.com.atomi.charge.course.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.course.model.entity.LessonEntity;

import java.util.List;

public interface LessonRepository extends BaseRepository<LessonEntity, String> {

    boolean existsByCourseIdAndCodeAndDeletedAtIsNull(String courseId, String code);

    boolean existsByCourseIdAndCodeAndIdNotAndDeletedAtIsNull(String courseId, String code, String id);

    Page<LessonEntity> findByCourseIdAndDeletedAtIsNull(String courseId, Pageable pageable);

    List<LessonEntity> findByCourseIdAndDeletedAtIsNullOrderByOrderIndexAscCreatedDateAsc(
            String courseId
    );
    long countByCourseIdAndDeletedAtIsNull(String courseId);
}
