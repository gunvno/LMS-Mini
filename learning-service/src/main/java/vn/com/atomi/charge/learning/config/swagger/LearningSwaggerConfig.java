package vn.com.atomi.charge.learning.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class LearningSwaggerConfig {

    @Bean
    @Primary
    public OpenAPI learningOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("LMS Learning Service API")
            .description("Enrollment, learning progress and certificate APIs")
            .version("1.0.0"));
    }
}
