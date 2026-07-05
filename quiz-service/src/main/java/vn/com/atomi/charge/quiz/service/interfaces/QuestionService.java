package vn.com.atomi.charge.quiz.service.interfaces;

import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.quiz.mapper.QuestionMapper;
import vn.com.atomi.charge.quiz.model.dto.QuestionDto;
import vn.com.atomi.charge.quiz.model.entity.QuestionEntity;
import vn.com.atomi.charge.quiz.repository.QuestionRepository;

public interface QuestionService extends IBaseService<QuestionRepository, QuestionDto, QuestionEntity, QuestionMapper> {
    BaseResponse<QuestionDto> createQuestion(BaseRequest<QuestionDto>dto, String QuizId);
}
