package vn.com.atomi.charge.billing.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.billing.model.dto.CourseDto;

@FeignClient(name = "lms-course-service")
public interface CourseClient {
    @GetMapping("/api/v1/courses/{id}/published")
    BaseResponse<CourseDto> getPublishedCourse(@PathVariable("id") String id);
}
