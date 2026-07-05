package vn.com.atomi.charge.quiz.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.quiz.model.entity.QuestionEntity;

public interface QuestionRepository extends BaseRepository<QuestionEntity, String> {
    boolean existsByQuizIdAndContentIgnoreCaseAndDeletedAtIsNull(String quizId, String content);

    boolean existsByQuizIdAndContentIgnoreCaseAndIdNotAndDeletedAtIsNull(String quizId, String content, String id);
}
