package vn.com.atomi.charge.quiz.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.quiz.model.dto.AnswerDto;
import vn.com.atomi.charge.quiz.model.dto.QuestionDto;
import vn.com.atomi.charge.quiz.service.interfaces.QuestionService;

@RestController
@RequestMapping("/questions")
@Tag(name = "Question", description = "CRUD APIs for question")
public class QuestionController extends BaseController<QuestionService,QuestionDto> {
    @PostMapping("/quizzes/{id}")
    public ResponseEntity<?> createQuestion(@PathVariable String id,
                                            @RequestBody @Valid BaseRequest<QuestionDto> request){
        return ResponseEntity.ok(service.createQuestion(request, id));
    }
}
