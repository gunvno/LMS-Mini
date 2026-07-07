package vn.com.atomi.charge.authn.service.interfaces;

import vn.com.atomi.charge.authn.mapper.AuthnUserMapper;
import vn.com.atomi.charge.authn.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.authn.model.request.InternalCreateUserRequest;
import vn.com.atomi.charge.authn.model.request.InternalResetPasswordRequest;
import vn.com.atomi.charge.authn.model.request.InternalUpdateUserStatusRequest;
import vn.com.atomi.charge.authn.repository.AuthnUserRepository;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.IBaseService;

import java.util.List;

public interface AuthnUserService extends IBaseService<AuthnUserRepository, AuthnUserDto, AuthnUserEntity, AuthnUserMapper>
{
    Boolean checkUser(String userId);
    BaseResponse<AuthnUserDto> getUserById(String userId);
    BaseResponse<List<AuthnUserDto>> getUsersByIds(List<String> userIds);
    BaseResponse<AuthnUserDto> createStaffUser(InternalCreateUserRequest request);
    BaseResponse<AuthnUserDto> updateUserStatus(String userId, InternalUpdateUserStatusRequest request);
    BaseResponse<AuthnUserDto> resetPassword(String userId, InternalResetPasswordRequest request);
}
