package vn.com.atomi.charge.notice.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class NoticeSwaggerConfig {

    @Bean
    @Primary
    public OpenAPI noticeOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("LMS Notice Service API")
            .description("In-app notice, device token and delivery log APIs")
            .version("1.0.0"));
    }
}
