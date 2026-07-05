package vn.com.atomi.charge.base.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;

@Configuration
@Slf4j
public class FeignConfig implements RequestInterceptor {
    @Bean
    public HttpMessageConverters httpMessageConverters() {
        return new HttpMessageConverters(new MappingJackson2HttpMessageConverter());
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        log.info("Feign url: {}", requestTemplate.path());
    }
}
