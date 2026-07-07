package vn.com.atomi.charge.quiz.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.quiz.model.entity.AnswerEntity;

import java.util.List;

public interface AnswerRepository extends BaseRepository<AnswerEntity, String> {

    boolean existsByQuestionIdAndContentIgnoreCaseAndDeletedAtIsNull(String questionId, String content);

    boolean existsByQuestionIdAndContentIgnoreCaseAndIdNotAndDeletedAtIsNull(
        String questionId,
        String content,
        String id
    );

    List<AnswerEntity> findByQuestionIdAndDeletedAtIsNullOrderByOrderIndexAsc(String questionId);
}
