package vn.com.atomi.charge.quiz.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;

import java.util.List;
import java.util.Collection;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface QuizRepository extends BaseRepository<QuizEntity,String > {
    List<QuizEntity> findByCourseIdAndRequiredToCompleteTrueAndStatusAndDeletedAtIsNull(
            String courseId,
            QuizStatus status
    );

    Page<QuizEntity> findByCourseIdInAndDeletedAtIsNull(Collection<String> courseIds, Pageable pageable);

    Page<QuizEntity> findByCourseIdInAndStatusAndDeletedAtIsNull(
            Collection<String> courseIds, QuizStatus status, Pageable pageable);

    Optional<QuizEntity> findFirstByLessonIdAndDeletedAtIsNull(String lessonId);
}
