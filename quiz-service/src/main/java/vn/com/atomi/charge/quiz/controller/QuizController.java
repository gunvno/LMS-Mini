package vn.com.atomi.charge.quiz.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.base.model.dto.BaseDto;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.quiz.model.dto.QuizDto;
import vn.com.atomi.charge.quiz.service.interfaces.QuizService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/quiz")
@Tag(name = "Quiz", description = "CRUD APIs for quiz")
public class QuizController extends BaseController<QuizService, QuizDto> {

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('QUIZ_VIEW')")
    public ResponseEntity<?> getAll(@RequestParam Map<String, String> params, Pageable pageable) {
        BaseResponse<Page<QuizDto>> dtos = service.getAll(params, pageable);
        return ResponseEntity.ok(dtos);
    }

    @Override
    @GetMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('QUIZ_VIEW')")
    public ResponseEntity<?> getDetails(@PathVariable("id") String id) {
        BaseResponse<QuizDto> dto = service.getDetails(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> create(@RequestBody @Valid BaseRequest<QuizDto> dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Override
    @PostMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    @Validated(BaseDto.Update.class)
    public ResponseEntity<?> update(@RequestBody @Valid BaseRequest<QuizDto> dto,
                                    @PathVariable String id) {
        dto.getData().setId(id);
        return ResponseEntity.ok(service.update(dto));
    }

    @Override
    @DeleteMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @Override
    @DeleteMapping
    @PreAuthorize("hasAuthority('QUIZ_MANAGE')")
    public ResponseEntity<?> deleteMany(@RequestBody List<String> ids) {
        return ResponseEntity.ok(service.delete(ids));
    }
}
