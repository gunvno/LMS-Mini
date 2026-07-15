package vn.com.atomi.charge.quiz.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.quiz.mapper.QuizAttemptMapper;
import vn.com.atomi.charge.quiz.model.dto.QuizAttemptAnswerInputDto;
import vn.com.atomi.charge.quiz.model.dto.QuizAttemptDto;
import vn.com.atomi.charge.quiz.model.dto.QuizAttemptHistoryDto;
import vn.com.atomi.charge.quiz.model.entity.AnswerEntity;
import vn.com.atomi.charge.quiz.model.entity.QuestionEntity;
import vn.com.atomi.charge.quiz.model.entity.QuizAttemptEntity;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizAttemptStatus;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;
import vn.com.atomi.charge.quiz.model.event.CourseCompletionEvaluationEvent;
import vn.com.atomi.charge.quiz.repository.AnswerRepository;
import vn.com.atomi.charge.quiz.repository.QuestionRepository;
import vn.com.atomi.charge.quiz.repository.QuizAttemptAnswerRepository;
import vn.com.atomi.charge.quiz.repository.QuizAttemptRepository;
import vn.com.atomi.charge.quiz.repository.QuizRepository;
import vn.com.atomi.charge.quiz.service.internal.QuizConfigurationValidator;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizAttemptServiceImplTest {

    @Mock
    private QuizAttemptRepository attemptRepository;
    @Mock
    private QuizRepository quizRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private QuizAttemptAnswerRepository attemptAnswerRepository;
    @Mock
    private QuizAttemptMapper attemptMapper;
    @Mock
    private QuizOwnershipService ownershipService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private QuizConfigurationValidator configurationValidator;

    private QuizAttemptServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new QuizAttemptServiceImpl();
        ReflectionTestUtils.setField(service, "repository", attemptRepository);
        ReflectionTestUtils.setField(service, "mapper", attemptMapper);
        ReflectionTestUtils.setField(service, "quizRepository", quizRepository);
        ReflectionTestUtils.setField(service, "questionRepository", questionRepository);
        ReflectionTestUtils.setField(service, "answerRepository", answerRepository);
        ReflectionTestUtils.setField(service, "quizAttemptAnswerRepository", attemptAnswerRepository);
        ReflectionTestUtils.setField(service, "ownershipService", ownershipService);
        ReflectionTestUtils.setField(service, "eventPublisher", eventPublisher);
        ReflectionTestUtils.setField(service, "configurationValidator", configurationValidator);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("student-1", null, List.of()));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldCalculateFullScoreAsOneHundredPercent() {
        BigDecimal score = QuizAttemptServiceImpl.calculatePercentageScore(
                new BigDecimal("10"),
                new BigDecimal("10"));

        assertThat(score).isEqualByComparingTo("100.00");
    }

    @Test
    void shouldCalculateWeightedScoreAsPercentage() {
        BigDecimal score = QuizAttemptServiceImpl.calculatePercentageScore(
                new BigDecimal("8"),
                new BigDecimal("10"));

        assertThat(score).isEqualByComparingTo("80.00");
    }

    @Test
    void shouldReturnZeroWhenQuizHasNoAvailableScore() {
        BigDecimal score = QuizAttemptServiceImpl.calculatePercentageScore(
                BigDecimal.ZERO,
                BigDecimal.ZERO);

        assertThat(score).isEqualByComparingTo("0.00");
    }

    @Test
    void shouldPassAtConfiguredThresholdAndFailBelowIt() {
        BigDecimal passScore = new BigDecimal("70");
        BigDecimal maximumScore = new BigDecimal("10");

        assertThat(QuizAttemptServiceImpl.hasPassed(new BigDecimal("70.00"), passScore, maximumScore)).isTrue();
        assertThat(QuizAttemptServiceImpl.hasPassed(new BigDecimal("69.99"), passScore, maximumScore)).isFalse();
    }

    @Test
    void shouldPassAttemptWhenAllAnswersAreCorrect() {
        QuizAttemptEntity attempt = new QuizAttemptEntity();
        attempt.setId("attempt-1");
        attempt.setQuizId("quiz-1");
        attempt.setUserId("student-1");
        attempt.setStatus(QuizAttemptStatus.IN_PROGRESS);

        QuizEntity quiz = new QuizEntity();
        quiz.setId("quiz-1");
        quiz.setCourseId("course-1");
        quiz.setPassScore(new BigDecimal("70"));
        quiz.setStatus(QuizStatus.ACTIVE);

        QuestionEntity firstQuestion = question("question-1", "2");
        QuestionEntity secondQuestion = question("question-2", "8");
        AnswerEntity firstAnswer = answer("answer-1", true);
        AnswerEntity secondAnswer = answer("answer-2", true);

        QuizAttemptDto submitted = new QuizAttemptDto();
        submitted.setAnswers(List.of(
                submittedAnswer("question-1", "answer-1"),
                submittedAnswer("question-2", "answer-2")));
        BaseRequest<QuizAttemptDto> request = new BaseRequest<>();
        request.setData(submitted);

        when(attemptRepository.findEntityById("attempt-1")).thenReturn(Optional.of(attempt));
        when(quizRepository.findEntityById("quiz-1")).thenReturn(Optional.of(quiz));
        when(configurationValidator.isValid("quiz-1")).thenReturn(true);
        when(questionRepository.findByQuizIdAndDeletedAtIsNullOrderByOrderIndexAsc("quiz-1"))
                .thenReturn(List.of(firstQuestion, secondQuestion));
        when(answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByOrderIndexAsc("question-1"))
                .thenReturn(List.of(firstAnswer));
        when(answerRepository.findByQuestionIdAndDeletedAtIsNullOrderByOrderIndexAsc("question-2"))
                .thenReturn(List.of(secondAnswer));
        when(attemptRepository.save(any(QuizAttemptEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(attemptMapper.toDto(any(QuizAttemptEntity.class))).thenAnswer(invocation -> {
            QuizAttemptEntity saved = invocation.getArgument(0);
            QuizAttemptDto dto = new QuizAttemptDto();
            dto.setScore(saved.getScore());
            dto.setPassed(saved.getPassed());
            return dto;
        });

        QuizAttemptDto result = service.submitQuiz("attempt-1", request).getData();

        assertThat(result.getScore()).isEqualByComparingTo("100.00");
        assertThat(result.getPassed()).isTrue();
        assertThat(result.getCorrectAnswers()).isEqualTo(2);
        assertThat(result.getTotalQuestions()).isEqualTo(2);
        verify(eventPublisher).publishEvent(any(CourseCompletionEvaluationEvent.class));
    }

    @Test
    void shouldReturnOnlyCurrentStudentsSubmittedAttemptsNewestFirst() {
        QuizEntity quiz = new QuizEntity();
        quiz.setId("quiz-1");
        quiz.setCourseId("course-1");
        quiz.setStatus(QuizStatus.ACTIVE);

        QuizAttemptEntity newest = submittedAttempt(
                "attempt-2",
                "85.50",
                true,
                LocalDateTime.of(2026, 7, 15, 9, 0));
        QuizAttemptEntity oldest = submittedAttempt(
                "attempt-1",
                "60.00",
                false,
                LocalDateTime.of(2026, 7, 14, 9, 0));

        when(quizRepository.findEntityById("quiz-1")).thenReturn(Optional.of(quiz));
        when(attemptRepository.findByQuizIdAndUserIdAndStatusAndScoreIsNotNullAndPassedIsNotNullAndSubmittedAtIsNotNullAndDeletedAtIsNullOrderBySubmittedAtDesc(
                "quiz-1", "student-1", QuizAttemptStatus.SUBMITTED))
                .thenReturn(List.of(newest, oldest));

        List<QuizAttemptHistoryDto> history = service.getMyAttemptHistory("quiz-1").getData();

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getId()).isEqualTo("attempt-2");
        assertThat(history.get(0).getAttemptNumber()).isEqualTo(2);
        assertThat(history.get(0).getScore()).isEqualByComparingTo("85.50");
        assertThat(history.get(0).getPassed()).isTrue();
        assertThat(history.get(1).getId()).isEqualTo("attempt-1");
        assertThat(history.get(1).getAttemptNumber()).isEqualTo(1);
        verify(ownershipService).assertCanViewQuiz(quiz);
    }

    private QuestionEntity question(String id, String score) {
        QuestionEntity question = new QuestionEntity();
        question.setId(id);
        question.setScore(new BigDecimal(score));
        return question;
    }

    private AnswerEntity answer(String id, boolean correct) {
        AnswerEntity answer = new AnswerEntity();
        answer.setId(id);
        answer.setCorrect(correct);
        return answer;
    }

    private QuizAttemptAnswerInputDto submittedAnswer(String questionId, String answerId) {
        QuizAttemptAnswerInputDto answer = new QuizAttemptAnswerInputDto();
        answer.setQuestionId(questionId);
        answer.setAnswerId(answerId);
        return answer;
    }

    private QuizAttemptEntity submittedAttempt(String id,
                                                String score,
                                                boolean passed,
                                                LocalDateTime submittedAt) {
        QuizAttemptEntity attempt = new QuizAttemptEntity();
        attempt.setId(id);
        attempt.setQuizId("quiz-1");
        attempt.setUserId("student-1");
        attempt.setScore(new BigDecimal(score));
        attempt.setPassed(passed);
        attempt.setStartedAt(submittedAt.minusMinutes(10));
        attempt.setSubmittedAt(submittedAt);
        attempt.setStatus(QuizAttemptStatus.SUBMITTED);
        return attempt;
    }
}
