package vn.com.atomi.charge.quiz.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vn.com.atomi.charge.quiz.model.event.CourseCompletionEvaluationEvent;
import vn.com.atomi.charge.quiz.repository.client.LearningClient;

@Component
@RequiredArgsConstructor
@Slf4j
public class CourseCompletionEventListener {

    private final LearningClient learningClient;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void evaluateCourseCompletion(CourseCompletionEvaluationEvent event) {
        try {
            learningClient.completeCourse(event.courseId());
        } catch (Exception exception) {
            log.warn("Could not evaluate course {} completion after quiz submission: {}",
                    event.courseId(), exception.getMessage());
        }
    }
}
