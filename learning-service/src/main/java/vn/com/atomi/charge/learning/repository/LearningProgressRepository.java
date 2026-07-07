package vn.com.atomi.charge.learning.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.learning.model.entity.LearningProgressEntity;

import java.util.Optional;

public interface LearningProgressRepository extends BaseRepository<LearningProgressEntity, String> {
    Optional<LearningProgressEntity> findByEnrollmentIdAndLessonIdAndDeletedAtIsNull(
            String enrollmentId,
            String lessonId);
}
