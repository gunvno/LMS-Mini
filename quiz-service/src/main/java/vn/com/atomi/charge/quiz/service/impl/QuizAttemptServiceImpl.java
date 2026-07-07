package vn.com.atomi.charge.quiz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.quiz.mapper.QuizAttemptMapper;
import vn.com.atomi.charge.quiz.model.dto.EnrollmentDto;
import vn.com.atomi.charge.quiz.model.dto.QuizAttemptDto;
import vn.com.atomi.charge.quiz.model.entity.QuizAttemptEntity;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizAttemptStatus;
import vn.com.atomi.charge.quiz.repository.QuizAttemptRepository;
import vn.com.atomi.charge.quiz.repository.QuizRepository;
import vn.com.atomi.charge.quiz.repository.client.LearningClient;
import vn.com.atomi.charge.quiz.service.interfaces.QuizAttemptService;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class QuizAttemptServiceImpl extends BaseService<QuizAttemptRepository, QuizAttemptDto,
        QuizAttemptEntity, QuizAttemptMapper> implements QuizAttemptService {
    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private LearningClient learningClient;
    @Override
    public BaseResponse<QuizAttemptDto> startQuiz(String QuizId){
        response=new BaseResponse<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        if(QuizId.isEmpty())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.not_found"));
        Optional<QuizEntity> optionalQuiz = quizRepository.findEntityById(QuizId);
        if(optionalQuiz.isEmpty())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.not_found"));
        BaseResponse<EnrollmentDto> enrollment = learningClient.findEnrollment(optionalQuiz.get().getCourseId());
        QuizAttemptEntity quizAttempt = new QuizAttemptEntity();
        quizAttempt.setQuizId(QuizId);
        quizAttempt.setUserId(userId);
        quizAttempt.setEnrollmentId(enrollment.getData().getId());
        quizAttempt.setStartedAt(LocalDateTime.now());
        quizAttempt.setStatus(QuizAttemptStatus.IN_PROGRESS);
        quizAttempt.setPassed(false);
        quizAttempt.setCreatedDate(LocalDateTime.now());
        quizAttempt.setCreatedBy(userId);

        QuizAttemptEntity saved = repository.save(quizAttempt);
        response.setData(mapper.toDto(saved));
        response.setStatus(HttpStatus.OK);
        return response;
    }
}
