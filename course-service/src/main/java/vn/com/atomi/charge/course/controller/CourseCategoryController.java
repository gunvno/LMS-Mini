package vn.com.atomi.charge.course.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.course.model.dto.CourseCategoryDto;
import vn.com.atomi.charge.course.service.interfaces.CourseCategoryService;

@RestController
@RequestMapping("/api/v1/course-categories")
@Tag(name = "Course Categories", description = "CRUD APIs for course categories")
public class CourseCategoryController
    extends BaseController<CourseCategoryService, CourseCategoryDto> {
}
