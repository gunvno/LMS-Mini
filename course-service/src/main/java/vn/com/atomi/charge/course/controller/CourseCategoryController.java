package vn.com.atomi.charge.course.controller;

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
import vn.com.atomi.charge.course.model.dto.CourseCategoryDto;
import vn.com.atomi.charge.course.service.interfaces.CourseCategoryService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/course-categories")
@Tag(name = "Course Categories", description = "CRUD APIs for course categories")
public class CourseCategoryController
    extends BaseController<CourseCategoryService, CourseCategoryDto> {

    @GetMapping("/catalog")
    @PreAuthorize("permitAll()")
    public ResponseEntity<?> getCatalogCategories() {
        return ResponseEntity.ok(service.getCatalogCategories());
    }

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('CATEGORY_VIEW')")
    public ResponseEntity<?> getAll(@RequestParam Map<String, String> params, Pageable pageable) {
        BaseResponse<Page<CourseCategoryDto>> dtos = service.getAll(params, pageable);
        return ResponseEntity.ok(dtos);
    }

    @Override
    @GetMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('CATEGORY_VIEW')")
    public ResponseEntity<?> getDetails(@PathVariable("id") String id) {
        BaseResponse<CourseCategoryDto> dto = service.getDetails(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE') and hasAuthority('COURSE_REVIEW')")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> create(@RequestBody @Valid BaseRequest<CourseCategoryDto> dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Override
    @PostMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE') and hasAuthority('COURSE_REVIEW')")
    @Validated(BaseDto.Update.class)
    public ResponseEntity<?> update(@RequestBody @Valid BaseRequest<CourseCategoryDto> dto,
                                    @PathVariable String id) {
        dto.getData().setId(id);
        return ResponseEntity.ok(service.update(dto));
    }

    @Override
    @DeleteMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE') and hasAuthority('COURSE_REVIEW')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @Override
    @DeleteMapping
    @PreAuthorize("hasAuthority('CATEGORY_MANAGE') and hasAuthority('COURSE_REVIEW')")
    public ResponseEntity<?> deleteMany(@RequestBody List<String> ids) {
        return ResponseEntity.ok(service.delete(ids));
    }
}
