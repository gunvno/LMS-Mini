package vn.com.atomi.charge.quiz.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.quiz.model.dto.AnswerDto;
import vn.com.atomi.charge.quiz.model.dto.QuestionDto;
import vn.com.atomi.charge.quiz.service.interfaces.QuestionService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/questions")
@Tag(name = "Question", description = "CRUD APIs for question")
public class QuestionController extends BaseController<QuestionService,QuestionDto> {
    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('QUIZ_VIEW')")
    public ResponseEntity<?> getAll(@RequestParam Map<String, String> params, Pageable pageable) {
        BaseResponse<Page<QuestionDto>> dtos = service.getAll(params, pageable);
        return ResponseEntity.ok(dtos);
    }

    @Override
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('QUIZ_VIEW')")
    public ResponseEntity<?> getDetails(@PathVariable String id) {
        return ResponseEntity.ok(service.getDetails(id));
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('QUESTION_MANAGE')")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> create(@RequestBody @Valid BaseRequest<QuestionDto> dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Override
    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('QUESTION_MANAGE')")
    @Validated(BaseDto.Update.class)
    public ResponseEntity<?> update(@RequestBody @Valid BaseRequest<QuestionDto> dto,
                                    @PathVariable String id) {
        dto.getData().setId(id);
        return ResponseEntity.ok(service.update(dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('QUESTION_MANAGE')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @Override
    @DeleteMapping
    @PreAuthorize("hasAuthority('QUESTION_MANAGE')")
    public ResponseEntity<?> deleteMany(@RequestBody List<String> ids) {
        return ResponseEntity.ok(service.delete(ids));
    }

    @PostMapping("/quizzes/{id}")
    @PreAuthorize("hasAuthority('QUESTION_MANAGE')")
    public ResponseEntity<?> createQuestion(@PathVariable String id,
                                            @RequestBody @Valid BaseRequest<QuestionDto> request){
        return ResponseEntity.ok(service.createQuestion(request, id));
    }
}
