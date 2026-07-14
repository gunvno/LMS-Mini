package vn.com.atomi.charge.quiz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.quiz.mapper.QuizAttemptMapper;
import vn.com.atomi.charge.quiz.model.dto.EnrollmentDto;
import vn.com.atomi.charge.quiz.model.dto.QuizAttemptAnswerInputDto;
import vn.com.atomi.charge.quiz.model.dto.QuizAttemptDto;
import vn.com.atomi.charge.quiz.model.entity.AnswerEntity;
import vn.com.atomi.charge.quiz.model.entity.QuestionEntity;
import vn.com.atomi.charge.quiz.model.entity.QuizAttemptEntity;
import vn.com.atomi.charge.quiz.model.entity.QuizAttemptAnswerEntity;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizAttemptStatus;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;
import vn.com.atomi.charge.quiz.model.event.CourseCompletionEvaluationEvent;
import vn.com.atomi.charge.quiz.repository.AnswerRepository;
import vn.com.atomi.charge.quiz.repository.QuestionRepository;
import vn.com.atomi.charge.quiz.repository.QuizAttemptRepository;
import vn.com.atomi.charge.quiz.repository.QuizAttemptAnswerRepository;
import vn.com.atomi.charge.quiz.repository.QuizRepository;
import vn.com.atomi.charge.quiz.client.LearningClient;
import vn.com.atomi.charge.quiz.service.interfaces.QuizAttemptService;
import vn.com.atomi.charge.quiz.service.internal.QuizConfigurationValidator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class QuizAttemptServiceImpl extends BaseService<QuizAttemptRepository, QuizAttemptDto,
        QuizAttemptEntity, QuizAttemptMapper> implements QuizAttemptService {
    @Autowired
    private QuizRepository quizRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Autowired
    private QuizAttemptAnswerRepository quizAttemptAnswerRepository;

    @Autowired
    private LearningClient learningClient;
    @Autowired
    private ApplicationEventPublisher eventPublisher;
    @Autowired
    private QuizOwnershipService ownershipService;
    @Autowired
    private QuizConfigurationValidator configurationValidator;
    @Override
    public BaseResponse<QuizAttemptDto> startQuiz(String QuizId){
        BaseResponse<QuizAttemptDto> response = new BaseResponse<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        if (QuizId == null || QuizId.isBlank())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.not_found"));
        Optional<QuizEntity> optionalQuiz = quizRepository.findEntityById(QuizId);
        if (optionalQuiz.isEmpty())
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.not_found"));

        QuizEntity quiz = optionalQuiz.get();
        ownershipService.assertCanViewQuiz(quiz);
        if (quiz.getStatus() != QuizStatus.ACTIVE) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.invalid_status"));
        }
        if (!configurationValidator.isValid(quiz.getId())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.invalid_configuration"));
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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<QuizAttemptDto> submitQuiz(String attemptId, BaseRequest<QuizAttemptDto> request) {
        BaseResponse<QuizAttemptDto> response = new BaseResponse<>();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();

        if (attemptId == null || attemptId.isBlank()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz_attempt.not_found"));
        }

        Optional<QuizAttemptEntity> optionalAttempt = repository.findEntityById(attemptId);
        if (optionalAttempt.isEmpty() || !userId.equals(optionalAttempt.get().getUserId())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz_attempt.not_found"));
        }

        QuizAttemptEntity attempt = optionalAttempt.get();
        if (attempt.getStatus() != QuizAttemptStatus.IN_PROGRESS) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz_attempt.invalid_status"));
        }

        Optional<QuizEntity> optionalQuiz = quizRepository.findEntityById(attempt.getQuizId());
        if (optionalQuiz.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.not_found"));
        }

        QuizEntity quiz = optionalQuiz.get();
        ownershipService.assertCanViewQuiz(quiz);
        if (quiz.getStatus() != QuizStatus.ACTIVE || !configurationValidator.isValid(quiz.getId())) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("quiz.invalid_configuration"));
        }
        List<QuestionEntity> questions = questionRepository.findByQuizIdAndDeletedAtIsNullOrderByOrderIndexAsc(quiz.getId());
        List<QuizAttemptAnswerInputDto> submittedAnswers = request == null || request.getData() == null || request.getData().getAnswers() == null
                ? Collections.emptyList()
                : request.getData().getAnswers();
        List<QuizAttemptAnswerEntity> attemptAnswers = new ArrayList<>();
        BigDecimal earnedScore = BigDecimal.ZERO;
        BigDecimal maximumScore = BigDecimal.ZERO;
        int correctAnswers = 0;

        for (QuestionEntity question : questions) {
            BigDecimal questionScore = positiveScore(question.getScore());
            maximumScore = maximumScore.add(questionScore);
            List<AnswerEntity> questionAnswers = answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByOrderIndexAsc(question.getId());
            QuizAttemptAnswerInputDto submittedItem = findSubmittedAnswer(submittedAnswers, question.getId());
            String selectedAnswerId = submittedItem == null || submittedItem.getAnswerId() == null
                    ? null
                    : submittedItem.getAnswerId().trim();

            if (selectedAnswerId == null || selectedAnswerId.isBlank()) {
                attemptAnswers.add(buildAttemptAnswer(attempt.getId(), question.getId(), null, false, BigDecimal.ZERO, userId));
                continue;
            }

            Optional<AnswerEntity> optionalSelectedAnswer = questionAnswers.stream()
                    .filter(answer -> selectedAnswerId.equals(answer.getId()))
                    .findFirst();
            if (optionalSelectedAnswer.isEmpty()) {
                return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("answer.not_found"));
            }

            AnswerEntity selectedAnswer = optionalSelectedAnswer.get();
            boolean isCorrect = Boolean.TRUE.equals(selectedAnswer.getCorrect());
            BigDecimal rowScore = isCorrect ? questionScore : BigDecimal.ZERO;
            earnedScore = earnedScore.add(rowScore);
            if (isCorrect) {
                correctAnswers++;
            }
            attemptAnswers.add(buildAttemptAnswer(attempt.getId(), question.getId(), selectedAnswerId, isCorrect, rowScore, userId));
        }

        quizAttemptAnswerRepository.saveAll(attemptAnswers);

        attempt.setScore(calculatePercentageScore(earnedScore, maximumScore));
        attempt.setPassed(hasPassed(attempt.getScore(), quiz.getPassScore(), maximumScore));
        attempt.setSubmittedAt(LocalDateTime.now());
        attempt.setStatus(QuizAttemptStatus.SUBMITTED);
        attempt.setLastModifiedBy(userId);
        attempt.setLastModifiedDate(LocalDateTime.now());

        QuizAttemptEntity saved = repository.save(attempt);
        if (Boolean.TRUE.equals(saved.getPassed())) {
            eventPublisher.publishEvent(new CourseCompletionEvaluationEvent(quiz.getCourseId()));
        }
        QuizAttemptDto result = mapper.toDto(saved);
        result.setTotalQuestions(questions.size());
        result.setCorrectAnswers(correctAnswers);
        response.setData(result);
        response.setStatus(HttpStatus.OK);
        return response;
    }

    static BigDecimal calculatePercentageScore(BigDecimal earnedScore, BigDecimal maximumScore) {
        if (earnedScore == null || maximumScore == null || maximumScore.signum() <= 0) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return earnedScore
                .multiply(BigDecimal.valueOf(100))
                .divide(maximumScore, 2, RoundingMode.HALF_UP);
    }

    static boolean hasPassed(BigDecimal percentageScore, BigDecimal passScore, BigDecimal maximumScore) {
        return percentageScore != null
                && passScore != null
                && maximumScore != null
                && maximumScore.signum() > 0
                && percentageScore.compareTo(passScore) >= 0;
    }

    private BigDecimal positiveScore(BigDecimal score) {
        return score != null && score.signum() > 0 ? score : BigDecimal.ZERO;
    }

    private QuizAttemptAnswerInputDto findSubmittedAnswer(List<QuizAttemptAnswerInputDto> submittedAnswers, String questionId) {
        if (submittedAnswers == null || submittedAnswers.isEmpty() || questionId == null || questionId.isBlank()) {
            return null;
        }

        for (QuizAttemptAnswerInputDto submittedAnswer : submittedAnswers) {
            if (submittedAnswer == null || submittedAnswer.getQuestionId() == null) {
                continue;
            }

            if (questionId.equals(submittedAnswer.getQuestionId().trim())) {
                return submittedAnswer;
            }
        }

        return null;
    }

    private QuizAttemptAnswerEntity buildAttemptAnswer(String attemptId,
                                                       String questionId,
                                                       String answerId,
                                                       boolean isCorrect,
                                                       BigDecimal score,
                                                       String username) {
        QuizAttemptAnswerEntity attemptAnswer = new QuizAttemptAnswerEntity();
        attemptAnswer.setAttemptId(attemptId);
        attemptAnswer.setQuestionId(questionId);
        attemptAnswer.setAnswerId(answerId);
        attemptAnswer.setCorrect(isCorrect);
        attemptAnswer.setScore(score);
        attemptAnswer.setCreatedBy(username);
        attemptAnswer.setLastModifiedBy(username);
        return attemptAnswer;
    }
}
