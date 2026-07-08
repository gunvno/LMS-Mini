package vn.com.atomi.charge.base.config.swagger;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.StringSchema;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@OpenAPIDefinition(
        info = @io.swagger.v3.oas.annotations.info.Info(title = "API Service", version = "1.0"),
        security = {@SecurityRequirement(name = "bearerAuth")}
)
@SecuritySchemes({
        @SecurityScheme(
                name = "bearerAuth",
                type = SecuritySchemeType.HTTP,
                scheme = "bearer",
                bearerFormat = "JWT"
        )
})
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI(@Value("${spring.application.name:}") String applicationName) {
        return new OpenAPI().info(new Info()
                .title("API Service")
                .description("API Service")
                .version("1.0.0"))
                .servers(List.of(new Server().url(resolveGatewayPrefix(applicationName))));
    }

    @Bean
    public OpenApiCustomizer gatewayServerCustomizer(@Value("${spring.application.name:}") String applicationName) {
        return openApi -> openApi.setServers(List.of(new Server().url(resolveGatewayPrefix(applicationName))));
    }

    private String resolveGatewayPrefix(String applicationName) {
        if (applicationName != null
                && applicationName.startsWith("lms-")
                && applicationName.endsWith("-service")) {
            return "/" + applicationName.substring(4, applicationName.length() - 8);
        }
        return "/";
    }

    @Bean
    public OperationCustomizer acceptLanguageHeaderCustomizer() {
      return (operation, handlerMethod) -> {
        operation.addParametersItem(
            new Parameter()
                .in(ParameterIn.HEADER.toString())
                .name("Accept-Language")
                .description("Language (vi, en, lo)")
                .required(false)
                .schema(new StringSchema()
                    ._enum(List.of("vi", "en", "lo"))
                    .example("vi"))
        );
        return operation;
      };
    }
}
