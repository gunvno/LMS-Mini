package vn.com.atomi.charge.quiz.service.interfaces;

import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.quiz.mapper.QuizMapper;
import vn.com.atomi.charge.quiz.model.dto.QuizDto;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.repository.QuizRepository;

public interface QuizService extends IBaseService<QuizRepository, QuizDto, QuizEntity, QuizMapper> {
}
