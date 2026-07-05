package vn.com.atomi.charge.authn.config.swagger;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AuthnSwaggerConfig {

    @Bean
    @Primary
    public OpenAPI authnOpenAPI() {
        return new OpenAPI().info(new Info()
            .title("LMS Authn Service API")
            .description("Authentication, user account, token and security APIs")
            .version("1.0.0"));
    }
}
