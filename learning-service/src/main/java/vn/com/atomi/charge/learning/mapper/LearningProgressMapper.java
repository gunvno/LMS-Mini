package vn.com.atomi.charge.learning.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.learning.model.dto.LearningProgressDto;
import vn.com.atomi.charge.learning.model.entity.LearningProgressEntity;

@Mapper(componentModel = "spring")
public interface LearningProgressMapper extends EntityMapper<String, LearningProgressDto, LearningProgressEntity> {
}
