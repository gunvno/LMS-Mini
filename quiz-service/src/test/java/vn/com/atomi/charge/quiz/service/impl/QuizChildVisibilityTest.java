package vn.com.atomi.charge.quiz.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.util.ReflectionTestUtils;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.quiz.mapper.AnswerMapper;
import vn.com.atomi.charge.quiz.mapper.QuestionMapper;
import vn.com.atomi.charge.quiz.model.dto.AnswerDto;
import vn.com.atomi.charge.quiz.model.dto.QuestionDto;
import vn.com.atomi.charge.quiz.model.entity.AnswerEntity;
import vn.com.atomi.charge.quiz.model.entity.QuestionEntity;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.repository.AnswerRepository;
import vn.com.atomi.charge.quiz.repository.QuestionRepository;
import vn.com.atomi.charge.quiz.repository.QuizRepository;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizChildVisibilityTest {

    @Mock
    private QuizRepository quizRepository;

    @Mock
    private QuestionRepository questionRepository;

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private QuestionMapper questionMapper;

    @Mock
    private AnswerMapper answerMapper;

    @Mock
    private QuizOwnershipService ownershipService;

    @Test
    void questionListCannotBypassPrivateCourseVisibility() {
        QuestionServiceImpl service = questionService();
        QuizEntity quiz = quiz("quiz-1", "course-private");
        when(quizRepository.findEntityById("quiz-1")).thenReturn(Optional.of(quiz));
        denyView(quiz);

        assertThrows(AccessDeniedException.class, () -> service.getAll(
                Map.of("quizId", "quiz-1"), PageRequest.of(0, 20)));
    }

    @Test
    void directAnswerViewCannotBypassPrivateCourseVisibility() {
        AnswerServiceImpl service = answerService();
        AnswerEntity answer = new AnswerEntity();
        answer.setId("answer-1");
        answer.setQuestionId("question-1");
        QuestionEntity question = question("question-1", "quiz-1");
        QuizEntity quiz = quiz("quiz-1", "course-private");
        when(answerRepository.findEntityById("answer-1")).thenReturn(Optional.of(answer));
        when(questionRepository.findEntityById("question-1")).thenReturn(Optional.of(question));
        when(quizRepository.findEntityById("quiz-1")).thenReturn(Optional.of(quiz));
        denyView(quiz);

        assertThrows(AccessDeniedException.class, () -> service.getDetails("answer-1"));
    }

    @Test
    void questionCreateCannotBypassPrivateCourseManagementCheck() {
        QuestionServiceImpl service = questionService();
        QuizEntity quiz = quiz("quiz-1", "course-private");
        QuestionDto dto = new QuestionDto();
        dto.setQuizId("quiz-1");
        when(quizRepository.findEntityById("quiz-1")).thenReturn(Optional.of(quiz));
        denyManage("course-private");

        assertThrows(AccessDeniedException.class, () -> service.create(request(dto)));
    }

    @Test
    void answerDeleteCannotBypassPrivateCourseManagementCheck() {
        AnswerServiceImpl service = answerService();
        AnswerEntity answer = new AnswerEntity();
        answer.setId("answer-1");
        answer.setQuestionId("question-1");
        QuestionEntity question = question("question-1", "quiz-1");
        QuizEntity quiz = quiz("quiz-1", "course-private");
        when(answerRepository.findEntityById("answer-1")).thenReturn(Optional.of(answer));
        when(questionRepository.findEntityById("question-1")).thenReturn(Optional.of(question));
        when(quizRepository.findEntityById("quiz-1")).thenReturn(Optional.of(quiz));
        denyManage("course-private");

        assertThrows(AccessDeniedException.class, () -> service.delete("answer-1"));
    }

    private QuestionServiceImpl questionService() {
        QuestionServiceImpl service = new QuestionServiceImpl();
        ReflectionTestUtils.setField(service, "repository", questionRepository);
        ReflectionTestUtils.setField(service, "mapper", questionMapper);
        ReflectionTestUtils.setField(service, "quizRepository", quizRepository);
        ReflectionTestUtils.setField(service, "ownershipService", ownershipService);
        return service;
    }

    private AnswerServiceImpl answerService() {
        AnswerServiceImpl service = new AnswerServiceImpl();
        ReflectionTestUtils.setField(service, "repository", answerRepository);
        ReflectionTestUtils.setField(service, "mapper", answerMapper);
        ReflectionTestUtils.setField(service, "questionRepository", questionRepository);
        ReflectionTestUtils.setField(service, "quizRepository", quizRepository);
        ReflectionTestUtils.setField(service, "ownershipService", ownershipService);
        return service;
    }

    private void denyView(QuizEntity quiz) {
        doThrow(new AccessDeniedException("common.access_denied"))
                .when(ownershipService).assertCanViewQuiz(quiz);
    }

    private void denyManage(String courseId) {
        doThrow(new AccessDeniedException("common.access_denied"))
                .when(ownershipService).assertCanManageCourse(courseId);
    }

    private static QuizEntity quiz(String id, String courseId) {
        QuizEntity quiz = new QuizEntity();
        quiz.setId(id);
        quiz.setCourseId(courseId);
        return quiz;
    }

    private static QuestionEntity question(String id, String quizId) {
        QuestionEntity question = new QuestionEntity();
        question.setId(id);
        question.setQuizId(quizId);
        return question;
    }

    private static <T extends vn.com.atomi.charge.base.model.dto.BaseDto> BaseRequest<T> request(T dto) {
        BaseRequest<T> request = new BaseRequest<>();
        request.setData(dto);
        return request;
    }
}
