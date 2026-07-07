package vn.com.atomi.charge.learning.repository.Client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "lms-quiz-service")
public interface QuizClient {
    @GetMapping("/internal/v1/quizzes/course/{courseId}/required-result")
    boolean completeQuizRequiredInCourse(@PathVariable String courseId);
}
