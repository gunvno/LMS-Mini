package vn.com.atomi.charge.course.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.com.atomi.charge.base.controller.BaseController;
import vn.com.atomi.charge.course.model.dto.ImageDto;
import vn.com.atomi.charge.course.service.interfaces.ImageService;

@RestController
@RequestMapping("/images")
@Tag(name = "Images", description = "CRUD APIs for images")
public class ImageController extends BaseController<ImageService, ImageDto> {
}
