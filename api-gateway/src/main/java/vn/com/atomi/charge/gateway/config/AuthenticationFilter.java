package vn.com.atomi.charge.gateway.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpCookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import vn.com.atomi.charge.gateway.dto.AuthnUserInfoDto;
import vn.com.atomi.charge.gateway.dto.BaseResponseDto;
import vn.com.atomi.charge.gateway.dto.IntrospectRequestDto;
import vn.com.atomi.charge.gateway.dto.IntrospectResponseDto;
import vn.com.atomi.charge.gateway.dto.UserDto;
import vn.com.atomi.charge.gateway.service.rest.AuthorClient;
import vn.com.atomi.charge.gateway.service.rest.AuthnClient;
import vn.com.atomi.charge.gateway.util.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthenticationFilter implements WebFilter {

  private final AuthnClient authnClient;
  private final AuthorClient authorClient;

  @Value("${internal.service-key:}")
  private String internalServiceKey;

  private static final String ACCESS_TOKEN_COOKIE = "lms_access_token";

  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();

    if (isPublicAuthPath(path)
            || path.startsWith("/actuator/")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")) {
      return chain.filter(exchange);
    }
    ServerHttpRequest request = exchange.getRequest();
    String token = resolveAccessToken(request);
    if (!StringUtils.hasText(token)) {
      return chain.filter(exchange);
    }

    return Mono.fromCallable(() -> resolveAuthenticatedUser(token))
        .subscribeOn(Schedulers.boundedElastic())
        .flatMap(resolved -> resolved.authenticated()
            ? filterAuthenticated(exchange, chain, resolved)
            : onError(exchange));
  }

  private Mono<Void> filterAuthenticated(ServerWebExchange exchange,
                                         WebFilterChain chain,
                                         AuthenticatedRequest resolved) {
    return forward(
        exchange,
        resolved.user(),
        resolved.encodedUser(),
        resolved.permissions(),
        resolved.context(),
        chain);
  }

  private AuthenticatedRequest resolveAuthenticatedUser(String token) {
    if (!isTokenValid(token)) {
      return AuthenticatedRequest.invalid();
    }
    try {
      UserDto user = getUserInfo(token);
      if (user == null) {
        return AuthenticatedRequest.invalid();
      }
      List<String> permissions = getUserPermissions(user.getId());
      user.setPermissions(permissions);
      UsernamePasswordAuthenticationToken authentication =
          new UsernamePasswordAuthenticationToken(user, null, toAuthorities(permissions));
      SecurityContext context = new SecurityContextImpl(authentication);
      String encodedUser = Base64.getEncoder()
          .encodeToString(JsonUtil.convertObjectToJson(user).getBytes(StandardCharsets.UTF_8));
      return new AuthenticatedRequest(true, user, permissions, context, encodedUser);
    } catch (Exception exception) {
      log.warn("Unable to resolve authenticated user: {}", exception.getMessage());
      return AuthenticatedRequest.invalid();
    }
  }

  private record AuthenticatedRequest(
      boolean authenticated,
      UserDto user,
      List<String> permissions,
      SecurityContext context,
      String encodedUser) {

    private static AuthenticatedRequest invalid() {
      return new AuthenticatedRequest(false, null, List.of(), null, null);
    }
  }

  private Mono<Void> forward(ServerWebExchange exchange, UserDto user, String encodedUser,
                             List<String> permissions, SecurityContext context,
                             WebFilterChain chain) {
    ServerHttpRequest newRequest = exchange.getRequest().mutate()
        .headers(headers -> {
          headers.set("X-User-Info", encodedUser);
          headers.set("X-User", user.getId());
          headers.set("X-Role-Code", user.getRoleCode());
          headers.set("X-Permissions", String.join(",", permissions));
          headers.set("X-Internal-Service-Key", internalServiceKey);
        })
        .build();

    ServerWebExchange newExchange = exchange.mutate()
        .request(newRequest)
        .build();

    log.info("Forward request: {}", newRequest.getURI());

    return chain.filter(newExchange)
        .contextWrite(
            ReactiveSecurityContextHolder.withSecurityContext(Mono.just(context))
        );
  }

  private Mono<Void> onError(ServerWebExchange exchange) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return response.setComplete();
  }

  private String resolveAccessToken(ServerHttpRequest request) {
    String authorization = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
    if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
      return authorization.substring(7);
    }
    HttpCookie cookie = request.getCookies().getFirst(ACCESS_TOKEN_COOKIE);
    return cookie == null ? null : cookie.getValue();
  }

  private boolean isPublicAuthPath(String path) {
    return path.equals("/auth/token")
        || path.equals("/auth/login")
        || path.equals("/auth/refresh")
        || path.equals("/auth/logout")
        || path.equals("/auth/register")
        || path.equals("/auth/otp-register")
        || path.equals("/auth/otp-verify")
        || path.startsWith("/auth/forgot-password/")
        || path.equals("/api/v1/auth/login")
        || path.equals("/api/v1/auth/refresh-token")
        || path.equals("/api/v1/auth/logout")
        || path.equals("/api/v1/auth/register")
        || path.equals("/api/v1/auth/otp-register")
        || path.equals("/api/v1/auth/otp-verify")
        || path.startsWith("/api/v1/auth/forgot-password/")
        || path.equals("/authn/api/v1/auth/login")
        || path.equals("/authn/api/v1/auth/refresh-token")
        || path.equals("/authn/api/v1/auth/logout")
        || path.equals("/authn/api/v1/auth/register")
        || path.equals("/authn/api/v1/auth/otp-register")
        || path.equals("/authn/api/v1/auth/otp-verify")
        || path.startsWith("/authn/api/v1/auth/forgot-password/");
  }

  private boolean isTokenValid(String token) {
    try {
      BaseResponseDto<IntrospectResponseDto> response =
          authnClient.introspect(new IntrospectRequestDto(token));
      return response != null && response.getData() != null && response.getData().isValid();
    } catch (Exception e) {
      return false;
    }
  }

  private UserDto getUserInfo(String token) {
    BaseResponseDto<AuthnUserInfoDto> response = authnClient.getUserInfo(token);
    if (response == null || response.getData() == null) {
      return null;
    }

    AuthnUserInfoDto authnUser = response.getData();
    UserDto user = new UserDto();
    user.setId(authnUser.getSub());
    user.setUsername(authnUser.getPreferred_username());
    user.setFullName(authnUser.getGiven_name());
    user.setDisplayName(authnUser.getGiven_name());
    return user;
  }

  private List<String> getUserPermissions(String userId) {
    if (!StringUtils.hasText(userId)) {
      return Collections.emptyList();
    }
    try {
      BaseResponseDto<List<String>> response = authorClient.getUserPermissions(userId);
      if (response == null || response.getData() == null) {
        return Collections.emptyList();
      }
      return response.getData().stream()
          .filter(StringUtils::hasText)
          .distinct()
          .toList();
    } catch (Exception e) {
      log.error("Get user permissions failed: {}", e.getMessage());
      return Collections.emptyList();
    }
  }

  private List<SimpleGrantedAuthority> toAuthorities(List<String> permissions) {
    if (permissions == null || permissions.isEmpty()) {
      return Collections.emptyList();
    }
    return permissions.stream()
        .filter(StringUtils::hasText)
        .distinct()
        .map(SimpleGrantedAuthority::new)
        .toList();
  }
}
