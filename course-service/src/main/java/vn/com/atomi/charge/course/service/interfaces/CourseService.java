package vn.com.atomi.charge.course.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.course.mapper.CourseMapper;
import vn.com.atomi.charge.course.model.dto.CourseDto;
import vn.com.atomi.charge.course.model.entity.CourseEntity;
import vn.com.atomi.charge.course.model.request.RejectCourseRequest;
import vn.com.atomi.charge.course.model.response.CourseCatalogResponse;
import vn.com.atomi.charge.course.repository.CourseRepository;

import java.util.List;

public interface CourseService
    extends IBaseService<CourseRepository, CourseDto, CourseEntity, CourseMapper> {

    BaseResponse<CourseDto> submitReview(String id);
    BaseResponse<CourseDto> approveCourse(String id);
    BaseResponse<CourseDto> rejectCourse(RejectCourseRequest request);
    BaseResponse<CourseDto> archiveCourse(String id);
    Boolean checkCourse(String courseId);
    Boolean checkPublishedCourse(String courseId);
    Boolean isInstructorOwner(String courseId, String userId);
    List<String> getInstructorCourseIds(String userId);
    Boolean isVisibleToReviewer(String courseId);
    List<String> getReviewerVisibleCourseIds();
    BaseResponse<Page<CourseDto>> getPublishedCourses(String categoryId, Pageable pageable);
    BaseResponse<CourseDto> getPublishedCourseDetails(String id);
    BaseResponse<List<CourseCatalogResponse>> getPublishedCatalog(int limit);
}
