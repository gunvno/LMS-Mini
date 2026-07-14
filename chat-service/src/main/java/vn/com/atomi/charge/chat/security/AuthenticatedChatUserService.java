package vn.com.atomi.charge.chat.security;

import com.nimbusds.jwt.SignedJWT;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.chat.client.AuthnClient;
import vn.com.atomi.charge.chat.model.dto.TokenIntrospectionDto;
import vn.com.atomi.charge.chat.model.exception.ChatException;
import vn.com.atomi.charge.chat.model.request.TokenIntrospectionRequest;

@Service
@RequiredArgsConstructor
public class AuthenticatedChatUserService {

    private final AuthnClient authnClient;

    public String requireUserId(String authorization) {
        String token = normalizeBearer(authorization);
        if (!StringUtils.hasText(token)) {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ");
        }
        try {
            BaseResponse<TokenIntrospectionDto> response = authnClient.introspect(new TokenIntrospectionRequest(token));
            if (response == null || response.getData() == null || !response.getData().valid()) {
                throw new ChatException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn");
            }
            String userId = SignedJWT.parse(token).getJWTClaimsSet().getSubject();
            if (!StringUtils.hasText(userId)) {
                throw new ChatException(HttpStatus.UNAUTHORIZED, "Không xác định được người dùng");
            }
            return userId;
        } catch (ChatException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ");
        }
    }

    private String normalizeBearer(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            return null;
        }
        return authorization.startsWith("Bearer ") ? authorization.substring(7) : authorization;
    }
}
