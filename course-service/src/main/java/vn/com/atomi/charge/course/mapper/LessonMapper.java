package vn.com.atomi.charge.course.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.course.model.dto.LessonDto;
import vn.com.atomi.charge.course.model.entity.LessonEntity;

@Mapper(componentModel = "spring")
public interface LessonMapper extends EntityMapper<String, LessonDto, LessonEntity> {
}
