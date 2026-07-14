package vn.com.atomi.charge.quiz.service.internal;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vn.com.atomi.charge.quiz.model.entity.AnswerEntity;
import vn.com.atomi.charge.quiz.model.entity.QuestionEntity;
import vn.com.atomi.charge.quiz.model.enums.QuestionType;
import vn.com.atomi.charge.quiz.repository.AnswerRepository;
import vn.com.atomi.charge.quiz.repository.QuestionRepository;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuizConfigurationValidator {

    private final QuestionRepository questionRepository;
    private final AnswerRepository answerRepository;

    public boolean isValid(String quizId) {
        List<QuestionEntity> questions = questionRepository
                .findByQuizIdAndDeletedAtIsNullOrderByOrderIndexAscCreatedDateAsc(quizId);
        if (questions.isEmpty()) {
            return false;
        }

        for (QuestionEntity question : questions) {
            if (question.getScore() == null || question.getScore().signum() <= 0) {
                return false;
            }
            List<AnswerEntity> answers = answerRepository
                    .findByQuestionIdAndDeletedAtIsNullOrderByOrderIndexAscCreatedDateAsc(question.getId());
            if (answers.size() < 2) {
                return false;
            }
            if (question.getQuestionType() == QuestionType.SINGLE_CHOICE
                    && answers.stream().filter(answer -> Boolean.TRUE.equals(answer.getCorrect())).count() != 1) {
                return false;
            }
        }
        return true;
    }
}
