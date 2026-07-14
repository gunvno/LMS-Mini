package vn.com.atomi.charge.chat.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.chat.model.dto.CourseCatalogDto;
import vn.com.atomi.charge.chat.model.dto.CourseDetailsDto;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "lms-course-service")
public interface CourseClient {
    @GetMapping("/internal/v1/courses/published-catalog")
    BaseResponse<List<CourseCatalogDto>> getPublishedCatalog(@RequestParam("limit") int limit);

    @GetMapping("/internal/v1/courses/{courseId}")
    BaseResponse<CourseDetailsDto> getCourse(@PathVariable String courseId);
}
