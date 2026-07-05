package vn.com.atomi.charge.authorization.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AuthorizationSwaggerConfig {

    @Bean
    @Primary
    public OpenAPI authorizationOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("LMS Authorization Service API")
            .description("Role and user-role APIs")
            .version("1.0.0"));
    }
}
