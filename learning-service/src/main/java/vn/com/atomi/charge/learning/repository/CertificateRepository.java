package vn.com.atomi.charge.learning.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.learning.model.entity.CertificateEntity;
import java.util.Optional;

public interface CertificateRepository extends BaseRepository<CertificateEntity, String> {
    Page<CertificateEntity> findByDeletedAtIsNull(Pageable pageable);

    Page<CertificateEntity> findByUserIdAndDeletedAtIsNull(
            String userId, Pageable pageable);

    boolean existsByCertificateCodeAndDeletedAtIsNull(String certificateCode);

    Optional<CertificateEntity> findByUserIdAndCourseIdAndDeletedAtIsNull(
            String userId, String courseId);

    Optional<CertificateEntity> findByCertificateCodeAndDeletedAtIsNull(String certificateCode);
}
