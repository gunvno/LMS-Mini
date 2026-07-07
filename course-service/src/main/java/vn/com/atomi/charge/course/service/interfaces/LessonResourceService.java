package vn.com.atomi.charge.course.service.interfaces;

import org.springframework.web.multipart.MultipartFile;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;
import vn.com.atomi.charge.course.mapper.LessonResourceMapper;
import vn.com.atomi.charge.course.model.dto.LessonResourceDto;
import vn.com.atomi.charge.course.model.entity.LessonResourceEntity;
import vn.com.atomi.charge.course.repository.LessonResourceRepository;

public interface LessonResourceService extends IBaseService<
    LessonResourceRepository,
    LessonResourceDto,
    LessonResourceEntity,
    LessonResourceMapper> {

    BaseResponse<LessonResourceDto> uploadLessonResource(
        String lessonId,
        MultipartFile file,
        String title,
        String externalUrl
    );
}
