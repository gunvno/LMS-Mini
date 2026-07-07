package vn.com.atomi.charge.quiz.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import vn.com.atomi.charge.base.service.BaseService;
import vn.com.atomi.charge.quiz.mapper.QuizMapper;
import vn.com.atomi.charge.quiz.model.dto.QuizDto;
import vn.com.atomi.charge.quiz.model.entity.QuizAttemptEntity;
import vn.com.atomi.charge.quiz.model.entity.QuizEntity;
import vn.com.atomi.charge.quiz.model.enums.QuizStatus;
import vn.com.atomi.charge.quiz.repository.QuizAttemptRepository;
import vn.com.atomi.charge.quiz.repository.QuizRepository;
import vn.com.atomi.charge.quiz.service.interfaces.QuizService;

import java.util.List;
import java.util.Optional;

@Service
public class QuizServiceImpl extends BaseService<QuizRepository, QuizDto, QuizEntity, QuizMapper>
implements QuizService {

    @Autowired
    private QuizAttemptRepository attemptRepository;
    @Override
    public boolean completeQuizRequiredInCourse(String courseId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        if (courseId == null || courseId.isBlank() || userId == null || userId.isBlank()) {
            return false;
        }

        List<QuizEntity> quizRequiredList =
                repository.findByCourseIdAndRequiredToCompleteTrueAndStatusAndDeletedAtIsNull(
                        courseId,
                        QuizStatus.ACTIVE
                );

        for (QuizEntity quiz : quizRequiredList) {
            boolean passed = attemptRepository
                    .existsByQuizIdAndUserIdAndPassedTrueAndDeletedAtIsNull(quiz.getId(), userId);

            if (!passed) {
                return false;
            }
        }

        return true;
    }}
