package vn.com.atomi.charge.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Slf4j
@Configuration
public class GlobalFiltersConfig {

  @Bean
  public GlobalFilter postGlobalFilter() {
    return (exchange, chain) -> chain.filter(exchange)
        .then(Mono.fromRunnable(() -> {
          log.info("Global Post Filter executed");
        }));
  }
}
