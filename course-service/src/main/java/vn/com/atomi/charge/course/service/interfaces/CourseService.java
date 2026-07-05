package vn.com.atomi.charge.course.service.interfaces;

import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.course.mapper.CourseMapper;
import vn.com.atomi.charge.course.model.dto.CourseDto;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.request.RejectCourseRequest;
import vn.com.atomi.charge.course.repository.CourseRepository;

public interface CourseService
    extends IBaseService<CourseRepository, CourseDto, CourseEntity, CourseMapper> {

    BaseResponse<CourseDto> submitReview(String id);
    BaseResponse<CourseDto> approveCourse(String id);
    BaseResponse<CourseDto> rejectCourse(RejectCourseRequest request);
    BaseResponse<CourseDto> archiveCourse(String id);
}
