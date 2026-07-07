package vn.com.atomi.charge.course.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.course.model.dto.LessonResourceDto;
import vn.com.atomi.charge.course.service.interfaces.LessonResourceService;

@RestController
@RequestMapping("/api/v1/lessons")
@Tag(name = "Lesson Resources", description = "Upload APIs for lesson resources")
public class LessonResourceUploadController {

    private final LessonResourceService lessonResourceService;

    public LessonResourceUploadController(LessonResourceService lessonResourceService) {
        this.lessonResourceService = lessonResourceService;
    }

    @PostMapping(value = "/{id}/resources", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('RESOURCE_MANAGE')")
    @Operation(summary = "Upload lesson resource", description = "Upload PDF/DOCX file or save an external resource URL")
    public BaseResponse<LessonResourceDto> uploadLessonResource(
        @PathVariable String id,
        @RequestParam(required = false) MultipartFile file,
        @RequestParam String title,
        @RequestParam(required = false) String externalUrl
    ) {
        return lessonResourceService.uploadLessonResource(id, file, title, externalUrl);
    }
}
