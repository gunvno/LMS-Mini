package vn.com.atomi.charge.learning.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.learning.model.entity.EnrollmentEntity;
import vn.com.atomi.charge.learning.model.enums.EnrollmentStatus;

import java.util.Optional;
import java.util.Collection;
import java.util.List;

import jakarta.persistence.LockModeType;

public interface EnrollmentRepository extends BaseRepository<EnrollmentEntity, String> {
    boolean existsByCourseIdAndUserIdAndDeletedAtIsNull(String courseId, String code);

    boolean existsByCourseIdAndUserIdAndIdNotAndDeletedAtIsNull(String courseId, String code, String id);

    Page<EnrollmentEntity> findByUserIdAndDeletedAtIsNull(String userId, Pageable pageable);

    Optional<EnrollmentEntity> findByUserIdAndCourseIdAndDeletedAtIsNull(String userId, String courseId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select enrollment
            from EnrollmentEntity enrollment
            where enrollment.userId = :userId
              and enrollment.courseId = :courseId
              and enrollment.deletedAt is null
            """)
    Optional<EnrollmentEntity> findForUpdateByUserIdAndCourseId(
            @Param("userId") String userId,
            @Param("courseId") String courseId);

    Page<EnrollmentEntity> findByCourseIdInAndDeletedAtIsNull(Collection<String> courseIds, Pageable pageable);

    List<EnrollmentEntity> findByUserIdAndStatusInAndDeletedAtIsNull(
            String userId, Collection<EnrollmentStatus> statuses);
}
