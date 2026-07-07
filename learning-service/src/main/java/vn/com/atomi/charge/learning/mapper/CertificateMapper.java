package vn.com.atomi.charge.learning.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.base.mapper.EntityMapper;
import vn.com.atomi.charge.learning.model.dto.CertificateDto;
import vn.com.atomi.charge.learning.model.entity.CertificateEntity;

@Mapper(componentModel = "spring")
public interface CertificateMapper extends EntityMapper<String, CertificateDto, CertificateEntity> {
}
