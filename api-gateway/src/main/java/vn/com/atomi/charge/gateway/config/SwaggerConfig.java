package vn.com.atomi.charge.gateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.models.parameters.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecuritySchemes;
import io.swagger.v3.oas.models.OpenAPI;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springdoc.core.customizers.OperationCustomizer;
import io.swagger.v3.oas.models.media.StringSchema;

import java.util.List;

@Configuration
@OpenAPIDefinition(
    info = @Info(title = "API Gateway", version = "1.0"),
    security = {@SecurityRequirement(name = "bearerAuth"), @SecurityRequirement(name = "apiKeyAuth")}
)
@SecuritySchemes({
    @SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
    ),
    @SecurityScheme(
        name = "apiKeyAuth",
        type = SecuritySchemeType.APIKEY,
        in = SecuritySchemeIn.HEADER,
        paramName = "X-API-KEY"
    )
})
public class SwaggerConfig {

  @Bean
  public OpenAPI customOpenAPI() {
    return new OpenAPI().info(new io.swagger.v3.oas.models.info.Info()
        .title("API Gateway Service")
        .description("API Gateway Service")
        .version("1.0.0"));
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
