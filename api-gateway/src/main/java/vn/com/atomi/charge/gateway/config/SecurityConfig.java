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
            "/*/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-*/v3/api-docs",
            "/actuator/**",
            "/swagger-resources/**",
            "/webjars/**",
            "/public/**",
            "/internal/**",
            "/api/v1/health-check",
            "/auth/register",
            "/auth/token",
            "/auth/login",
            "/auth/introspect",
            "/auth/refresh",
            "/auth/logout",
            "/auth/otp-register",
            "/auth/otp-verify",
            "/authn/api/v1/auth/register",
            "/authn/api/v1/auth/login",
            "/authn/api/v1/auth/refresh-token",
            "/authn/api/v1/auth/otp-register",
            "/authn/api/v1/auth/otp-verify",
            "/authn/api/v1/.well-known/jwks.json",
            "/course/api/v1/courses/published",
            "/course/api/v1/courses/*/published",
            "/course/api/v1/courses/*/images/primary/view"
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
