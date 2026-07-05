package vn.com.atomi.charge.course.service.interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.com.atomi.charge.base.model.request.BaseRequest;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.course.mapper.LessonMapper;
import vn.com.atomi.charge.course.model.dto.LessonDto;
import vn.com.atomi.charge.course.model.entity.LessonEntity;
import vn.com.atomi.charge.course.repository.LessonRepository;

public interface LessonService
    extends IBaseService<LessonRepository, LessonDto, LessonEntity, LessonMapper> {
    BaseResponse<LessonDto> createLesson(BaseRequest<LessonDto> dto, String courseId);
    BaseResponse<Page<LessonDto>> getLessonByCourseId(String courseId, Pageable pageable);
}
