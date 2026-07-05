package vn.com.atomi.charge.quiz.service.interfaces;

import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.quiz.mapper.AnswerMapper;
import vn.com.atomi.charge.quiz.model.dto.AnswerDto;
import vn.com.atomi.charge.quiz.model.entity.AnswerEntity;
import vn.com.atomi.charge.quiz.repository.AnswerRepository;

public interface AnswerService extends IBaseService<AnswerRepository, AnswerDto, AnswerEntity, AnswerMapper> {
    BaseResponse<AnswerDto> createAnswer(BaseRequest<AnswerDto> dto, String questionId);
}
