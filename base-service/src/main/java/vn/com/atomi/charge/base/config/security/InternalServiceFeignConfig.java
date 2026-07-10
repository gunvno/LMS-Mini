package vn.com.atomi.charge.base.config.security;

import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

@Configuration
public class InternalServiceFeignConfig {

    @Bean
    public RequestInterceptor internalServiceKeyInterceptor(
            @Value("${internal.service-key:}") String internalServiceKey) {
        return requestTemplate -> {
            if (StringUtils.hasText(internalServiceKey)) {
                requestTemplate.header("X-Internal-Service-Key", internalServiceKey);
            }
        };
    }
}
