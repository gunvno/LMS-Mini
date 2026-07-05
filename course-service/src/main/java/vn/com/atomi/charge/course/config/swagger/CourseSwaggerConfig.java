package vn.com.atomi.charge.course.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class CourseSwaggerConfig {

    @Bean
    @Primary
    public OpenAPI courseOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("LMS Course Service API")
            .description("Course, lesson, resource and image APIs")
            .version("1.0.0"));
    }
}
