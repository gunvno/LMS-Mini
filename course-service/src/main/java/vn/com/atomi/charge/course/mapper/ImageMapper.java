package vn.com.atomi.charge.course.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.course.model.dto.ImageDto;
import vn.com.atomi.charge.course.model.entity.ImageEntity;

@Mapper(componentModel = "spring")
public interface ImageMapper extends EntityMapper<String, ImageDto, ImageEntity> {
}
