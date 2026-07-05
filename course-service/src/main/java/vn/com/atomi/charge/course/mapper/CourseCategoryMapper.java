package vn.com.atomi.charge.course.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.course.model.dto.CourseCategoryDto;
import vn.com.atomi.charge.course.model.entity.CourseCategoryEntity;

@Mapper(componentModel = "spring")
public interface CourseCategoryMapper
    extends EntityMapper<String, CourseCategoryDto, CourseCategoryEntity> {
}
