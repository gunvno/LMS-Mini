package vn.com.atomi.charge.base.config.feign;

import feign.RequestInterceptor;
import feign.RequestTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import vn.com.atomi.charge.base.model.enums.CustomHeader;

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
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return;
        }

        String authorization = attributes.getRequest().getHeader(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authorization)) {
            requestTemplate.header(HttpHeaders.AUTHORIZATION, authorization);
        }
        forwardHeader(requestTemplate, attributes, CustomHeader.USER_INFO.getHeaderName());
        forwardHeader(requestTemplate, attributes, CustomHeader.PHONE_NUMBER.getHeaderName());
        forwardHeader(requestTemplate, attributes, CustomHeader.ROLE_CODE.getHeaderName());
        forwardHeader(requestTemplate, attributes, CustomHeader.PERMISSIONS.getHeaderName());
    }

    private void forwardHeader(RequestTemplate requestTemplate, ServletRequestAttributes attributes, String headerName) {
        String value = attributes.getRequest().getHeader(headerName);
        if (StringUtils.hasText(value)) {
            requestTemplate.header(headerName, value);
        }
    }
}
