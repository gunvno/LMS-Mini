package vn.com.atomi.charge.quiz.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.quiz.model.dto.AnswerDto;
import vn.com.atomi.charge.quiz.service.impl.AnswerServiceImpl;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/answers")
@Tag(name = "Answer", description = "CRUD APIs for answer")
public class AnswerController extends BaseController<AnswerServiceImpl, AnswerDto> {

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('QUIZ_VIEW')")
    public ResponseEntity<?> getAll(@RequestParam Map<String, String> params, Pageable pageable) {
        BaseResponse<Page<AnswerDto>> dtos = service.getAll(params, pageable);
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
    @PreAuthorize("hasAuthority('ANSWER_MANAGE')")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> create(@RequestBody @Valid BaseRequest<AnswerDto> dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Override
    @PostMapping("/{id}")
    @PreAuthorize("hasAuthority('ANSWER_MANAGE')")
    @Validated(BaseDto.Update.class)
    public ResponseEntity<?> update(@RequestBody @Valid BaseRequest<AnswerDto> dto,
                                    @PathVariable String id) {
        dto.getData().setId(id);
        return ResponseEntity.ok(service.update(dto));
    }

    @Override
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ANSWER_MANAGE')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @Override
    @DeleteMapping
    @PreAuthorize("hasAuthority('ANSWER_MANAGE')")
    public ResponseEntity<?> deleteMany(@RequestBody List<String> ids) {
        return ResponseEntity.ok(service.delete(ids));
    }

    @PostMapping("/questions/{id}")
    @PreAuthorize("hasAuthority('ANSWER_MANAGE')")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> createAnswer(@PathVariable String id,
                                          @RequestBody @Valid BaseRequest<AnswerDto> request) {
        return ResponseEntity.ok(service.createAnswer(request, id));
    }
}
