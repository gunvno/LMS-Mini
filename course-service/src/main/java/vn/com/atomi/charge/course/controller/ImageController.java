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
import vn.com.atomi.charge.course.model.dto.ImageDto;
import vn.com.atomi.charge.course.service.interfaces.ImageService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/images")
@Tag(name = "Images", description = "CRUD APIs for images")
public class ImageController extends BaseController<ImageService, ImageDto> {

    @Override
    @GetMapping
    @PreAuthorize("hasAuthority('IMAGE_VIEW')")
    public ResponseEntity<?> getAll(@RequestParam Map<String, String> params, Pageable pageable) {
        BaseResponse<Page<ImageDto>> dtos = service.getAll(params, pageable);
        return ResponseEntity.ok(dtos);
    }

    @Override
    @GetMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('IMAGE_VIEW')")
    public ResponseEntity<?> getDetails(@PathVariable("id") String id) {
        BaseResponse<ImageDto> dto = service.getDetails(id);
        return ResponseEntity.ok(dto);
    }

    @Override
    @PostMapping
    @PreAuthorize("hasAuthority('IMAGE_MANAGE')")
    @Validated(BaseDto.Create.class)
    public ResponseEntity<?> create(@RequestBody @Valid BaseRequest<ImageDto> dto) {
        return ResponseEntity.ok(service.create(dto));
    }

    @Override
    @PostMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('IMAGE_MANAGE')")
    @Validated(BaseDto.Update.class)
    public ResponseEntity<?> update(@RequestBody @Valid BaseRequest<ImageDto> dto,
                                    @PathVariable String id) {
        dto.getData().setId(id);
        return ResponseEntity.ok(service.update(dto));
    }

    @Override
    @DeleteMapping(value = {"/{id}"})
    @PreAuthorize("hasAuthority('IMAGE_MANAGE')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        return ResponseEntity.ok(service.delete(id));
    }

    @Override
    @DeleteMapping
    @PreAuthorize("hasAuthority('IMAGE_MANAGE')")
    public ResponseEntity<?> deleteMany(@RequestBody List<String> ids) {
        return ResponseEntity.ok(service.delete(ids));
    }
}
