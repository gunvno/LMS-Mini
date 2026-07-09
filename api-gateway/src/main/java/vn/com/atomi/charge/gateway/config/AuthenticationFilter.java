package vn.com.atomi.charge.gateway.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.*;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.lang.Nullable;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import vn.com.atomi.charge.gateway.dto.AuthnUserInfoDto;
import vn.com.atomi.charge.gateway.dto.BaseResponseDto;
import vn.com.atomi.charge.gateway.dto.IntrospectRequestDto;
import vn.com.atomi.charge.gateway.dto.IntrospectResponseDto;
import vn.com.atomi.charge.gateway.dto.SecurityRequestDto;
import vn.com.atomi.charge.gateway.dto.UserDto;
import vn.com.atomi.charge.gateway.service.rest.AuthorClient;
import vn.com.atomi.charge.gateway.dto.VerifySignRequestDto;
import vn.com.atomi.charge.gateway.service.rest.AuthnClient;
import vn.com.atomi.charge.gateway.util.JsonUtil;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class AuthenticationFilter implements WebFilter {

  @Autowired
  AuthnClient authnClient;

  @Autowired
  AuthorClient authorClient;

  @Value("${spring.profiles.active}")
  private String profile;

  ObjectMapper mapper = new ObjectMapper();

  private static final byte[] EMPTY_BODY = new byte[0];

  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
    String path = exchange.getRequest().getURI().getPath();

    if (path.equals("/auth/token")
            || path.startsWith("/auth/")
            || path.startsWith("/api/v1/auth/")
            || path.startsWith("/authn/api/v1/auth/")
            || path.startsWith("/actuator/")
            || path.startsWith("/swagger-ui")
            || path.startsWith("/v3/api-docs")) {
      return chain.filter(exchange);
    }
    ServerHttpRequest request = exchange.getRequest();
    if (!isAuthHeader(request)) {
      return chain.filter(exchange);
    }

    String authHeader = getAuthHeader(request);
    if (!StringUtils.hasText(authHeader) || !authHeader.startsWith("Bearer ")) {
      return chain.filter(exchange);
    }

    String token = authHeader.substring(7);
    // Use feign client call to auth service verify token at here
    boolean isValidate = isTokenValid(token);
    if (!isValidate) {
      return this.onError(exchange);
    }
    UserDto user;
    try {
      user = getUserInfo(token);
    } catch (Exception e) {
      return onError(exchange);
    }
    if (user == null) {
      return chain.filter(exchange);
    }
    List<String> permissions = getUserPermissions(user.getId());
    user.setPermissions(permissions);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(user, null, toAuthorities(permissions));

    SecurityContext context = new SecurityContextImpl(authentication);

    String encodedUser = Base64.getEncoder()
        .encodeToString(JsonUtil.convertObjectToJson(user).getBytes(StandardCharsets.UTF_8));

    // POST → need read body
    if (request.getMethod() == HttpMethod.POST) {
      return DataBufferUtils.join(request.getBody()).flatMap(buffer -> {
        byte[] bodyBytes = new byte[buffer.readableByteCount()];
        buffer.read(bodyBytes);
        DataBufferUtils.release(buffer);
        MediaType contentType = request.getHeaders().getContentType();
        Flux<DataBuffer> cachedFlux = Flux.defer(() -> {
          DataBuffer cacheBuffer = exchange.getResponse().bufferFactory().wrap(bodyBytes);
          return Mono.just(cacheBuffer);
        });

        if (isTextContent(contentType)) {
          String bodyString = new String(bodyBytes, StandardCharsets.UTF_8);
          if (!StringUtils.hasText(bodyString)) {
            return forward(exchange, request, null, user, encodedUser, permissions, context, chain);
          }
          SecurityRequestDto requestDto;
          try {
            requestDto = mapper.readValue(bodyString, SecurityRequestDto.class);
          } catch (Exception e) {
            log.error("Invalid JSON body", e);
            return onError(exchange, HttpStatus.BAD_REQUEST);
          }

          if (!"APP".equalsIgnoreCase(requestDto.getChannel())) {
            return forward(exchange, request, cachedFlux, user, encodedUser, permissions, context, chain);
          }

          String deviceId = extractDeviceId(requestDto.getData());
          Gson gson = new GsonBuilder()
            .serializeNulls()
            .create();
          String dataJson = gson.toJson(requestDto.getData());

          VerifySignRequestDto signDto = new VerifySignRequestDto();
          signDto.setDeviceId(deviceId);
          signDto.setSignature(requestDto.getSignature());
          signDto.setData(dataJson);

          if (!isValidSign(signDto)) {
            log.error("Invalid signature for {}", exchange.getRequest().getURI());
            return onError(exchange, HttpStatus.BAD_REQUEST);
          }
        }

        return forward(exchange, request, cachedFlux, user, encodedUser, permissions, context, chain);
      }).switchIfEmpty(
          forward(exchange, user, encodedUser, permissions, context, chain)
      );
    }

    // NON-POST
    return forward(exchange, user, encodedUser, permissions, context, chain);
  }

  private boolean isTextContent(MediaType contentType) {
    if (contentType == null) return false;

    return MediaType.APPLICATION_JSON.includes(contentType)
        || MediaType.APPLICATION_XML.includes(contentType)
        || MediaType.APPLICATION_FORM_URLENCODED.includes(contentType)
        || contentType.getType().equalsIgnoreCase("text");
  }

  private String extractDeviceId(Object data) {
    if (data instanceof Map<?, ?> map) {
      Object v = map.get("deviceId");
      return v != null ? v.toString() : "";
    }
    return "";
  }

  // POST (has body)
  private Mono<Void> forward(ServerWebExchange exchange, ServerHttpRequest baseRequest,
                             @Nullable Flux<DataBuffer> body,
                             UserDto user, String encodedUser, List<String> permissions,
                             SecurityContext context, WebFilterChain chain) {

    ServerHttpRequest request = (body == null) ? baseRequest : new ServerHttpRequestDecorator(baseRequest) {
      @Override
      public Flux<DataBuffer> getBody() {
        return body;
      }
    };

    ServerHttpRequest newRequest = request.mutate()
        .header("X-User-Info", encodedUser)
        .header("X-User", user.getId())
        .header("X-Role-Code", user.getRoleCode())
        .header("X-Permissions", String.join(",", permissions))
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

  // NON-POST
  private Mono<Void> forward(ServerWebExchange exchange, UserDto user, String encodedUser,
                             List<String> permissions, SecurityContext context, WebFilterChain chain) {
    return forward(exchange, exchange.getRequest(), null, user, encodedUser, permissions, context, chain);
  }

  private Mono<Void> onError(ServerWebExchange exchange) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);
    return response.setComplete();
  }

  private Mono<Void> onError(ServerWebExchange exchange, HttpStatus status) {
    ServerHttpResponse response = exchange.getResponse();
    if (response.isCommitted()) {
      log.warn("Response already committed, cannot write error");
      return Mono.empty();
    }
    response.setStatusCode(status);
    response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
    String body = String.format("{\"code\":\"%s\"}", status.name());
    DataBuffer buffer = response.bufferFactory()
        .wrap(body.getBytes(StandardCharsets.UTF_8));
    return response.writeWith(Mono.just(buffer));
  }

  private boolean isValidSign(VerifySignRequestDto data) {
    return true;
  }

  private String getAuthHeader(ServerHttpRequest request) {
    return request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
  }

  private boolean isAuthHeader(ServerHttpRequest request) {
    return request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
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
