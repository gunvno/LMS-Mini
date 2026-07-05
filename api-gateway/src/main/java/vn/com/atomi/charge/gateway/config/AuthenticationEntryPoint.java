package vn.com.atomi.charge.gateway.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthenticationEntryPoint implements ServerAuthenticationEntryPoint {
    @Override
    public Mono<Void> commence(ServerWebExchange exchange, AuthenticationException ex) {
        // Get the response object
        ServerHttpResponse response = exchange.getResponse();

        // Set the HTTP status code to unauthorized
        response.setStatusCode(HttpStatus.UNAUTHORIZED);

        // Set the response body with the error message
        return response.setComplete();
    }
}
