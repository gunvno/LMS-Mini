package vn.com.atomi.charge.quiz.service.interfaces;

import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.quiz.mapper.QuizAttemptMapper;
import vn.com.atomi.charge.quiz.model.dto.QuizAttemptDto;
import vn.com.atomi.charge.quiz.model.entity.QuizAttemptEntity;
import vn.com.atomi.charge.quiz.repository.QuizAttemptRepository;

public interface QuizAttemptService extends IBaseService<QuizAttemptRepository, QuizAttemptDto, QuizAttemptEntity, QuizAttemptMapper> {
    BaseResponse<QuizAttemptDto> startQuiz(String QuizId);
}
