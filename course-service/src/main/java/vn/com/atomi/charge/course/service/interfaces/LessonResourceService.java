package vn.com.atomi.charge.course.service.interfaces;

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
}
