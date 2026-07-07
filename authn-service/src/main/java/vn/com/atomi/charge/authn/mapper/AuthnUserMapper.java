package vn.com.atomi.charge.authn.mapper;

import org.mapstruct.Mapper;
import vn.com.atomi.charge.authn.model.dto.AuthnDto;
import vn.com.atomi.charge.authn.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.base.mapper.EntityMapper;

@Mapper(componentModel = "spring")
public interface AuthnUserMapper extends EntityMapper<String, AuthnUserDto, AuthnUserEntity> {
}
