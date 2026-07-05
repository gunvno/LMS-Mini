package vn.com.atomi.charge.course.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.course.model.dto.LessonResourceDto;
import vn.com.atomi.charge.course.model.entity.LessonResourceEntity;

@Mapper(componentModel = "spring")
public interface LessonResourceMapper
    extends EntityMapper<String, LessonResourceDto, LessonResourceEntity> {
}
