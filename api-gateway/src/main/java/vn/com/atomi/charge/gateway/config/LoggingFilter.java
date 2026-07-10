package vn.com.atomi.charge.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;

import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

  @Override
  public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
    long start = System.currentTimeMillis();
    var request = exchange.getRequest();
    // Do not log raw query values or headers: they can contain tokens, cookies, passwords and OTPs.
    log.info("=>>> INCOMING REQUEST: {} {} | queryKeys={} | headerNames={}",
        request.getMethod(), request.getURI().getRawPath(),
        request.getQueryParams().keySet(), request.getHeaders().keySet());
    return chain.filter(exchange).then(Mono.fromRunnable(() -> {
      long end = System.currentTimeMillis();
      long callTime = end - start;
      var response = exchange.getResponse();
      log.info("=>>> RESPONSE {} at: {} totalTime:{} ms", response.getStatusCode(), end, callTime);
    }));
  }

  @Override
  public int getOrder() {
    return Ordered.HIGHEST_PRECEDENCE;
  }
}
