package vn.com.atomi.charge.authn.service.interfaces;

import vn.com.atomi.charge.authn.mapper.AuthnUserMapper;
import vn.com.atomi.charge.authn.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.authn.repository.AuthnUserRepository;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;

public interface AuthnUserService extends IBaseService<AuthnUserRepository, AuthnUserDto, AuthnUserEntity, AuthnUserMapper>
{
    Boolean checkUser(String userId);
    BaseResponse<AuthnUserDto> getUserById(String userId);
}
