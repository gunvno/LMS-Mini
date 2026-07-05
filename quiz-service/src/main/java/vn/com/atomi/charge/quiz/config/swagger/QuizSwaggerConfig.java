package vn.com.atomi.charge.quiz.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class QuizSwaggerConfig {

    @Bean
    @Primary
    public OpenAPI quizOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("LMS Quiz Service API")
            .description("Quiz, question, answer and quiz attempt APIs")
            .version("1.0.0"));
    }
}
