package vn.com.atomi.charge.quiz.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.quiz.model.dto.AnswerDto;
import vn.com.atomi.charge.quiz.service.impl.AnswerServiceImpl;

@RestController
@RequestMapping("/answers")
@Tag(name = "Answer", description = "CRUD APIs for answer")
public class AnswerController extends BaseController<AnswerServiceImpl, AnswerDto> {

    @PostMapping("/questions/{id}")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> createAnswer(@PathVariable String id,
                                          @RequestBody @Valid BaseRequest<AnswerDto> request) {
        return ResponseEntity.ok(service.createAnswer(request, id));
    }
}
