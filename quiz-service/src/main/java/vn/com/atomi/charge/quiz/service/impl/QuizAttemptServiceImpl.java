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
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;
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
        response = new BaseResponse<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        if (QuizId == null || QuizId.isBlank())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.not_found"));
        Optional<QuizEntity> optionalQuiz = quizRepository.findEntityById(QuizId);
        if (optionalQuiz.isEmpty())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.not_found"));

        QuizEntity quiz = optionalQuiz.get();
        if (quiz.getStatus() != QuizStatus.ACTIVE) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.invalid_status"));
        }

        BaseResponse<EnrollmentDto> enrollmentResponse = learningClient.findEnrollment(optionalQuiz.get().getCourseId());
        if (enrollmentResponse == null || enrollmentResponse.getData() == null
                || enrollmentResponse.getData().getId() == null
                || !userId.equals(enrollmentResponse.getData().getUserId())
                || !quiz.getCourseId().equals(enrollmentResponse.getData().getCourseId())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("learning.not_found"));
        }

        Optional<QuizAttemptEntity> inProgressAttempt = repository
                .findFirstByQuizIdAndUserIdAndStatusAndDeletedAtIsNullOrderByStartedAtDesc(
                        QuizId,
                        userId,
                        QuizAttemptStatus.IN_PROGRESS);
        if (inProgressAttempt.isPresent()) {
            response.setData(mapper.toDto(inProgressAttempt.get()));
            response.setStatus(HttpStatus.OK);
            return response;
        }

        long attemptCount = repository.countByQuizIdAndUserIdAndDeletedAtIsNull(QuizId, userId);
        Integer maxAttempts = quiz.getMaxAttempts();
        if (maxAttempts != null && maxAttempts > 0 && attemptCount >= maxAttempts) {
            return BaseResponse.fail(HttpStatus.CONFLICT, i18n.getMessage("quiz.attempt_limit_reached"));
        }

        QuizAttemptEntity quizAttempt = new QuizAttemptEntity();
        quizAttempt.setQuizId(QuizId);
        quizAttempt.setUserId(userId);
        quizAttempt.setEnrollmentId(enrollmentResponse.getData().getId());
        quizAttempt.setStartedAt(LocalDateTime.now());
        quizAttempt.setStatus(QuizAttemptStatus.IN_PROGRESS);
        quizAttempt.setPassed(false);
        quizAttempt.setCreatedBy(userId);

        QuizAttemptEntity saved = repository.save(quizAttempt);
        response.setData(mapper.toDto(saved));
        response.setStatus(HttpStatus.OK);
        return response;
    }
}
