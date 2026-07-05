package vn.com.atomi.charge.course.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.course.model.dto.CourseDto;
import vn.com.atomi.charge.course.model.entity.CourseEntity;

@Mapper(componentModel = "spring")
public interface CourseMapper extends EntityMapper<String, CourseDto, CourseEntity> {
}
