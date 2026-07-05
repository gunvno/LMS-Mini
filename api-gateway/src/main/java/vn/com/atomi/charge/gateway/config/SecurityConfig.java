package vn.com.atomi.charge.gateway.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebFluxSecurity
@EnableMethodSecurity(securedEnabled = true)
@RequiredArgsConstructor
public class SecurityConfig {

    private final AuthenticationFilter authFilter;

    private final AuthenticationEntryPoint authEntryPoint;

    private final String[] WHITELIST_API = {
            "/swagger-ui.html",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-*/v3/api-docs",
            "/actuator/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/public/**",
            "/internal/**",
            "/authn/api/v1/register/**",
            "/authn/api/v1/confirm-otp",
            "/authn/api/v1/resend-otp",
            "/authn/api/v1/login",
            "/authn/internal/v1/verify/create-password",
            "/authn/internal/v1/verify/generate-key",
            "/authn/internal/v1/verify/sign-data",
            "/authn/internal/v1/verify/signature",
            "/authn/internal/v1/verify/user-info",
            "/authn/api/v1/reset-password",
            "/authn/api/v1/send-otp-reset-password",
            "/authn/api/v1/confirm-otp-reset-password",
            "/authn/api/v1/.well-known/jwks.json",
            "/location/**",
            "/charge/api/v1/station/map?**",
            "/charge/api/v1/station/info/**",
            "/charge/api/v1/transaction/charging-soc",
            "/charge/api/v1/transaction/preparing",
            "/charge/api/v1/transaction/authorize",
            "/charge/api/v1/transaction/after-authorize",
            "/charge/api/v1/transaction/after-stop",
            "/device/api/v1/mobile/init-app",
            "/notification/internal/v1/received",
            "/payment/api/v1/*/return",
            "/charge/cpo/api/versions/**",
            "/charge/emsp/api/versions/**",
            "/charge/cpo/api/2.2.1/**",
            "/charge/emsp/api/2.2.1/**",
            "/charge/ocpi/**",
            "/api/v1/health-check"
    };

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .exceptionHandling(e -> e.authenticationEntryPoint(authEntryPoint))
                .authorizeExchange(auth -> auth
                        .pathMatchers(WHITELIST_API).permitAll()
                        .anyExchange().authenticated())
                .addFilterBefore(authFilter, SecurityWebFiltersOrder.AUTHENTICATION)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(List.of("*")); // Allow specific origins
        config.setAllowedMethods(List.of("*"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public ServerCodecConfigurer serverCodecConfigurer() {
      ServerCodecConfigurer configurer = ServerCodecConfigurer.create();
      configurer.defaultCodecs().maxInMemorySize(200 * 1024 * 1024);
      return configurer;
    }
}
