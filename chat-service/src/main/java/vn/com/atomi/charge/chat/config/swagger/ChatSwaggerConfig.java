package vn.com.atomi.charge.chat.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class ChatSwaggerConfig {
    @Bean
    @Primary
    public OpenAPI chatOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("LMS Chat Service API")
                .description("Anonymous AI course assistant with REST commands and WebSocket/STOMP events")
                .version("1.0.0"));
    }
}
