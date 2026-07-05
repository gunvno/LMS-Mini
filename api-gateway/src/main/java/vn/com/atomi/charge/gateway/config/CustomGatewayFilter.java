package vn.com.atomi.charge.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class CustomGatewayFilter implements GatewayFilter {
  private static final Logger log = LoggerFactory.getLogger(CustomGatewayFilter.class);

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    return chain.filter(exchange)
        .doOnError(error -> log.error("Filter error: ", error))
        .onErrorResume(error -> {
          exchange.getResponse().setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
          return exchange.getResponse().setComplete();
        });
  }
}