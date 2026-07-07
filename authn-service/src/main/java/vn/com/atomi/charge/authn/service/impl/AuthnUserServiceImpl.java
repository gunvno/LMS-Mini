package vn.com.atomi.charge.authn.service.impl;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.authn.mapper.AuthnUserMapper;
import vn.com.atomi.charge.authn.model.dto.AuthnUserDto;
import vn.com.atomi.charge.authn.model.entity.AuthnUserEntity;
import vn.com.atomi.charge.authn.repository.AuthnUserRepository;
import vn.com.atomi.charge.authn.service.interfaces.AuthnUserService;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.base.service.BaseService;

import java.util.Optional;

@Service
public class AuthnUserServiceImpl extends BaseService<AuthnUserRepository, AuthnUserDto, AuthnUserEntity, AuthnUserMapper>
        implements AuthnUserService {

    @Override
    public Boolean checkUser(String userId) {
        if (!StringUtils.hasText(userId)) {
            return false;
        }
        Optional<AuthnUserEntity> optionalAuthnUser = repository.findEntityById(userId);
        return optionalAuthnUser.isPresent();
    }

    @Override
    public BaseResponse<AuthnUserDto> getUserById(String userId) {
        response = new BaseResponse<>();
        Optional<AuthnUserEntity> optionalUser = repository.findEntityById(userId);
        if (optionalUser.isEmpty()) {
            return BaseResponse.fail(HttpStatus.BAD_REQUEST, i18n.getMessage("user.not_found"));
        }
        AuthnUserEntity entity = optionalUser.get();
        response.setData(mapper.toDto(entity));
        response.setStatus(HttpStatus.OK);
        return response;
    }
}
