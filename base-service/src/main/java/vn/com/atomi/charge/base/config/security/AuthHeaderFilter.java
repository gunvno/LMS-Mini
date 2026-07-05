package vn.com.atomi.charge.base.config.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.annotation.Nonnull;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.security.SecurityProperties;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.com.atomi.charge.base.model.enums.CustomHeader;
import vn.com.atomi.charge.base.util.JsonUtil;
import vn.com.atomi.charge.base.util.Util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
@Order(SecurityProperties.BASIC_AUTH_ORDER)
@Slf4j
@RequiredArgsConstructor
public class AuthHeaderFilter extends OncePerRequestFilter {

//    private final AuthnFeignService authnFeignService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, @Nonnull HttpServletResponse response, @Nonnull FilterChain filterChain) throws ServletException, IOException {
//        String encodedUser = request.getHeader(CustomHeader.USER_INFO.getHeaderName());
//        if (StringUtils.hasText(encodedUser)) {
//            try {
//                UserDto user = Util.decodeBase64ToObject(encodedUser, UserDto.class);
//                List<SimpleGrantedAuthority> authorities = Collections.emptyList();
//                Authentication auth = new UsernamePasswordAuthenticationToken(
//                        user, null, authorities); // authorities nếu cần
//                SecurityContextHolder.getContext().setAuthentication(auth);
//            } catch (Exception e) {
//                log.error(e.getMessage());
//            }
//        } else {
//            String authHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
//            if (authHeader != null && authHeader.startsWith("Bearer ")) {
//                try {
//                    String token = authHeader.substring(7); // Bỏ chữ "Bearer "
//                    String[] parts = token.split("\\.");
//                    String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]));
//                    ObjectMapper mapper = new ObjectMapper();
//                    mapper.registerModule(new JavaTimeModule());
//                    Map<String, Object> claims = mapper.readValue(payloadJson, Map.class);
//                    log.info("doFilterInternal User : " + claims.get("sub").toString() + " role: " + claims.get("roleName").toString());
//                    UserDto user = authnFeignService.getUserInfo(token);
//                    List<SimpleGrantedAuthority> authorities = Collections.emptyList();
//                    Authentication auth = new UsernamePasswordAuthenticationToken(
//                            user, null, authorities); // authorities nếu cần
//                    SecurityContextHolder.getContext().setAuthentication(auth);
//
//                    MutableHttpServletRequest mutableRequest = new MutableHttpServletRequest(request);
//                    String userJson = JsonUtil.convertObjectToJson(user);
//                    encodedUser = Base64.getEncoder().encodeToString(userJson.getBytes(StandardCharsets.UTF_8));
//                    mutableRequest.putHeader("X-User-Info", encodedUser);
//                    mutableRequest.putHeader("X-User", claims.get("sub").toString());
//                    mutableRequest.putHeader("X-Role-Code", claims.get("roleName").toString());
//                    filterChain.doFilter(mutableRequest, response);
//                    return;
//                } catch (Exception e) {
//                    log.error("authUserFail");
//                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
//                    return;
//                }
//            }
//        }
        filterChain.doFilter(request, response);
    }
}
