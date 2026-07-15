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

import java.util.Locale;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class AuthenticatedChatUserService {

    private static final Set<String> SUPPORTED_PORTALS = Set.of("ADMIN", "STUDENT");

    private final AuthnClient authnClient;

    public String requireUserId(String authorization) {
        String token = normalizeBearer(authorization);
        if (!StringUtils.hasText(token)) {
            throw new ChatException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ");
        }
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            String portal = jwt.getJWTClaimsSet().getStringClaim("portal");
            portal = StringUtils.hasText(portal) ? portal.trim().toUpperCase(Locale.ROOT) : null;
            if (!SUPPORTED_PORTALS.contains(portal)) {
                throw new ChatException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập không hợp lệ");
            }
            BaseResponse<TokenIntrospectionDto> response = authnClient.introspect(
                    portal,
                    new TokenIntrospectionRequest(token));
            if (response == null || response.getData() == null || !response.getData().valid()) {
                throw new ChatException(HttpStatus.UNAUTHORIZED, "Phiên đăng nhập đã hết hạn");
            }
            String userId = jwt.getJWTClaimsSet().getSubject();
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
