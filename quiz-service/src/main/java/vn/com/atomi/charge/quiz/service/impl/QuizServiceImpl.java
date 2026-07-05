package vn.com.atomi.charge.quiz.service.impl;

import org.springframework.stereotype.Service;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.quiz.mapper.QuizMapper;
import vn.com.atomi.charge.quiz.model.dto.QuizDto;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.repository.QuizRepository;
import vn.com.atomi.charge.quiz.service.interfaces.QuizService;

@Service
public class QuizServiceImpl extends BaseService<QuizRepository, QuizDto, QuizEntity, QuizMapper>
implements QuizService {
}
