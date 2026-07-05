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
import vn.com.atomi.charge.gateway.dto.SecurityRequestDto;
import vn.com.atomi.charge.gateway.dto.UserDto;
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

  @Value("${spring.profiles.active}")
  private String profile;

  ObjectMapper mapper = new ObjectMapper();

  private static final byte[] EMPTY_BODY = new byte[0];

  @Override
  @NonNull
  public Mono<Void> filter(ServerWebExchange exchange, @NonNull WebFilterChain chain) {
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
      user = authnClient.getUserInfo(token);
    } catch (Exception e) {
      return onError(exchange);
    }
    if (user == null) {
      return chain.filter(exchange);
    }

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList());

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
            return forward(exchange, request, null, user, encodedUser, context, chain);
          }
          SecurityRequestDto requestDto;
          try {
            requestDto = mapper.readValue(bodyString, SecurityRequestDto.class);
          } catch (Exception e) {
            log.error("Invalid JSON body", e);
            return onError(exchange, HttpStatus.BAD_REQUEST);
          }

          if (!"APP".equalsIgnoreCase(requestDto.getChannel())) {
            return forward(exchange, request, cachedFlux, user, encodedUser, context, chain);
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

        return forward(exchange, request, cachedFlux, user, encodedUser, context, chain);
      }).switchIfEmpty(
          forward(exchange, user, encodedUser, context, chain)
      );
    }

    // NON-POST
    return forward(exchange, user, encodedUser, context, chain);
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
                             UserDto user, String encodedUser, SecurityContext context, WebFilterChain chain) {

    ServerHttpRequest request = (body == null) ? baseRequest : new ServerHttpRequestDecorator(baseRequest) {
      @Override
      public Flux<DataBuffer> getBody() {
        return body;
      }
    };

    ServerHttpRequest newRequest = request.mutate()
        .header("X-User-Info", encodedUser)
        .header("X-User", user.getUsername())
        .header("X-Role-Code", user.getRoleCode())
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
  private Mono<Void> forward(ServerWebExchange exchange, UserDto user, String encodedUser, SecurityContext context, WebFilterChain chain) {
    return forward(exchange, exchange.getRequest(), null, user, encodedUser, context, chain);
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
    try {
      ResponseEntity<Boolean> response = authnClient.verifySign(data);
      return response.getBody() != null && response.getBody();
    } catch (Exception e) {
      return false;
    }
  }

  private String getAuthHeader(ServerHttpRequest request) {
    return request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
  }

  private boolean isAuthHeader(ServerHttpRequest request) {
    return request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION);
  }

  private boolean isTokenValid(String token) {
    try {
      ResponseEntity<Boolean> response = authnClient.validateToken(token);
      return response.getBody() != null && response.getBody();
    } catch (Exception e) {
      return false;
    }
  }
}
