package vn.com.atomi.charge.base.config.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.com.atomi.charge.base.i18n.IMessageService;
import vn.com.atomi.charge.base.model.enums.CustomHeader;
import vn.com.atomi.charge.base.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Order(SecurityProperties.BASIC_AUTH_ORDER)
@Slf4j
@RequiredArgsConstructor
public class AuthHeaderFilter extends OncePerRequestFilter {

    private final IMessageService messageService;

    @Value("${internal.service-key:}")
    private String internalServiceKey;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String encodedUser = request.getHeader(CustomHeader.USER_INFO.getHeaderName());
        String userId = request.getHeader(CustomHeader.PHONE_NUMBER.getHeaderName());
        String permissions = request.getHeader(CustomHeader.PERMISSIONS.getHeaderName());

        if (StringUtils.hasText(encodedUser)) {
            String providedKey = request.getHeader(CustomHeader.INTERNAL_SERVICE_KEY.getHeaderName());
            if (!secureEquals(internalServiceKey, providedKey)) {
                SecurityContextHolder.clearContext();
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"status\":\"FORBIDDEN\",\"message\":\""
                        + messageService.getMessage("security.access_denied") + "\"}");
                return;
            }
            try {
                Map<?, ?> userInfo = Util.decodeBase64ToObject(encodedUser, Map.class);
                String principal = StringUtils.hasText(userId) ? userId : resolvePrincipal(userInfo);
                List<SimpleGrantedAuthority> authorities = parseAuthorities(permissions);
                Authentication auth = new UsernamePasswordAuthenticationToken(
                        principal, userInfo, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception e) {
                log.error("Decode user header failed: {}", e.getMessage());
                SecurityContextHolder.clearContext();
            }
        }
        filterChain.doFilter(request, response);
    }

    private boolean secureEquals(String expected, String actual) {
        if (!StringUtils.hasText(expected) || !StringUtils.hasText(actual)) {
            return false;
        }
        return MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                actual.getBytes(StandardCharsets.UTF_8));
    }

    private String resolvePrincipal(Map<?, ?> userInfo) {
        Object id = userInfo.get("id");
        if (id != null && StringUtils.hasText(id.toString())) {
            return id.toString();
        }
        Object sub = userInfo.get("sub");
        if (sub != null && StringUtils.hasText(sub.toString())) {
            return sub.toString();
        }
        Object username = userInfo.get("username");
        return username == null ? null : username.toString();
    }

    private List<SimpleGrantedAuthority> parseAuthorities(String permissions) {
        if (!StringUtils.hasText(permissions)) {
            return Collections.emptyList();
        }
        return Arrays.stream(permissions.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .distinct()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }
}
