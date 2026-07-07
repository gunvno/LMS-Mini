package vn.com.atomi.charge.base.config.security;

import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.com.atomi.charge.base.model.enums.CustomHeader;
import vn.com.atomi.charge.base.util.Util;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Order(SecurityProperties.BASIC_AUTH_ORDER)
@Slf4j
@RequiredArgsConstructor
public class AuthHeaderFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
        String encodedUser = request.getHeader(CustomHeader.USER_INFO.getHeaderName());
        String userId = request.getHeader(CustomHeader.PHONE_NUMBER.getHeaderName());
        String permissions = request.getHeader(CustomHeader.PERMISSIONS.getHeaderName());

        if (StringUtils.hasText(encodedUser)) {
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
