package vn.com.atomi.charge.learning.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.learning.model.dto.EnrollmentDto;
import vn.com.atomi.charge.learning.model.entity.EnrollmentEntity;

@Mapper(componentModel = "spring")
public interface EnrollmentMapper extends EntityMapper<String, EnrollmentDto, EnrollmentEntity> {
}
