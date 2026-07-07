package vn.com.atomi.charge.quiz.repository;

import vn.com.atomi.charge.base.repository.BaseRepository;
import vn.com.atomi.charge.quiz.model.entity.QuizAttemptEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizAttemptStatus;

import java.util.Optional;

public interface QuizAttemptRepository extends BaseRepository<QuizAttemptEntity, String> {
	long countByQuizIdAndUserIdAndDeletedAtIsNull(String quizId, String userId);

	Optional<QuizAttemptEntity> findFirstByQuizIdAndUserIdAndStatusAndDeletedAtIsNullOrderByStartedAtDesc(
			String quizId,
			String userId,
			QuizAttemptStatus status);
	boolean existsByQuizIdAndUserIdAndPassedTrueAndDeletedAtIsNull(String quizId, String userId);
}
