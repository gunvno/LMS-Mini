package vn.com.atomi.charge.quiz.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;

import java.util.List;

public interface QuizRepository extends BaseRepository<QuizEntity,String > {
    List<QuizEntity> findByCourseIdAndRequiredToCompleteTrueAndStatusAndDeletedAtIsNull(
            String courseId,
            QuizStatus status
    );
}
