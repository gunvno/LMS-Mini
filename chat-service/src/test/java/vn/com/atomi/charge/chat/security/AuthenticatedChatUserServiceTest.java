package vn.com.atomi.charge.chat.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.junit.jupiter.api.Test;
import vn.com.atomi.charge.base.model.response.BaseResponse;
import vn.com.atomi.charge.chat.client.AuthnClient;
import vn.com.atomi.charge.chat.model.dto.TokenIntrospectionDto;
import vn.com.atomi.charge.chat.model.exception.ChatException;
import vn.com.atomi.charge.chat.model.request.TokenIntrospectionRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuthenticatedChatUserServiceTest {

    private final AuthnClient authnClient = mock(AuthnClient.class);
    private final AuthenticatedChatUserService service = new AuthenticatedChatUserService(authnClient);

    @Test
    void forwardsVerifiedTokenPortalToIntrospection() throws Exception {
        String token = token("user-a", "student");
        TokenIntrospectionRequest request = new TokenIntrospectionRequest(token);
        when(authnClient.introspect("STUDENT", request))
                .thenReturn(BaseResponse.success(org.springframework.http.HttpStatus.OK, new TokenIntrospectionDto(true)));

        assertThat(service.requireUserId("Bearer " + token)).isEqualTo("user-a");

        verify(authnClient).introspect("STUDENT", request);
    }

    @Test
    void rejectsMissingOrUnsupportedPortalBeforeIntrospection() throws Exception {
        assertThatThrownBy(() -> service.requireUserId(token("user-a", null)))
                .isInstanceOf(ChatException.class);
        assertThatThrownBy(() -> service.requireUserId(token("user-a", "PARTNER")))
                .isInstanceOf(ChatException.class);

        verifyNoInteractions(authnClient);
    }

    private static String token(String subject, String portal) throws Exception {
        JWTClaimsSet.Builder claims = new JWTClaimsSet.Builder().subject(subject);
        if (portal != null) {
            claims.claim("portal", portal);
        }
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS256), claims.build());
        jwt.sign(new MACSigner("01234567890123456789012345678901"));
        return jwt.serialize();
    }
}
