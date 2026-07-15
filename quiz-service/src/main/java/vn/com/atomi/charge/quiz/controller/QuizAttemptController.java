package vn.com.atomi.charge.quiz.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.quiz.model.dto.QuizAttemptDto;
import vn.com.atomi.charge.quiz.service.interfaces.QuizAttemptService;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAuthority('QUIZ_ATTEMPT')")
public class QuizAttemptController {

    private final QuizAttemptService service;

    public QuizAttemptController(QuizAttemptService service) {
        this.service = service;
    }

    @PostMapping("/quizzes/{id}/attempts")
    @PreAuthorize("hasAuthority('QUIZ_ATTEMPT')")
    public ResponseEntity<?> startQuiz(@PathVariable String id){
        return ResponseEntity.ok(service.startQuiz(id));
    }

    @PostMapping("/quiz-attempts/{id}/submit")
    @PreAuthorize("hasAuthority('QUIZ_ATTEMPT')")
    public ResponseEntity<?> submitQuiz(@PathVariable String id,
                                        @RequestBody @Valid BaseRequest<QuizAttemptDto> request) {
        return ResponseEntity.ok(service.submitQuiz(id, request));
    }

    @GetMapping("/quizzes/{id}/attempts/me")
    @PreAuthorize("hasAuthority('QUIZ_ATTEMPT')")
    public ResponseEntity<?> getMyAttemptHistory(@PathVariable String id) {
        return ResponseEntity.ok(service.getMyAttemptHistory(id));
    }
}
