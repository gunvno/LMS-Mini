package vn.com.atomi.charge.learning.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.learning.model.entity.EnrollmentEntity;
import vn.com.atomi.charge.learning.model.enums.EnrollmentStatus;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

public interface EnrollmentRepository extends BaseRepository<EnrollmentEntity, String> {
    boolean existsByCourseIdAndUserIdAndDeletedAtIsNull(String courseId, String code);

    boolean existsByCourseIdAndUserIdAndIdNotAndDeletedAtIsNull(String courseId, String code, String id);

    Page<EnrollmentEntity> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

    Optional<EnrollmentEntity> findByUserIdAndCourseIdAndDeletedAtIsNull(String userId, String courseId);

    Page<EnrollmentEntity> findByCourseIdInAndDeletedAtIsNull(Collection<String> courseIds, Pageable pageable);

    List<EnrollmentEntity> findByUserIdAndStatusInAndDeletedAtIsNull(
            String userId, Collection<EnrollmentStatus> statuses);
}
