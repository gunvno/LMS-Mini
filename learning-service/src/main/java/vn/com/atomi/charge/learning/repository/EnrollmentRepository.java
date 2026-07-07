package vn.com.atomi.charge.learning.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.learning.model.entity.EnrollmentEntity;

import java.util.Optional;

public interface EnrollmentRepository extends BaseRepository<EnrollmentEntity, String> {
    boolean existsByCourseIdAndUserIdAndDeletedAtIsNull(String courseId, String code);

    boolean existsByCourseIdAndUserIdAndIdNotAndDeletedAtIsNull(String courseId, String code, String id);

    Page<EnrollmentEntity> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

    Optional<EnrollmentEntity> findByUserIdAndCourseIdAndDeletedAtIsNull(String userId, String courseId);
}
